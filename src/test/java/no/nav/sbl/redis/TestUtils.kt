package no.nav.sbl.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.internal.HostAndPort
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import no.nav.sbl.redis.TestUtils.WithRedis.Companion.PASSWORD
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.testcontainers.containers.GenericContainer
import java.time.Duration
import java.util.*

object TestUtils {
    class RedisContainer : GenericContainer<RedisContainer>("redis:6-alpine") {
        init {
            withCommand("redis-server --requirepass $PASSWORD")
            withExposedPorts(6379)
        }
    }

    class RedisSubscriber(
        private val subscribeCallback: () -> Unit = {},
    ) : RedisPubSubAdapter<String, String>() {
        val messages: MutableList<Pair<String, String>> = mutableListOf()
        private val lock = WaitLock(false)

        override fun message(
            pattern: String,
            channel: String,
            message: String,
        ) {
            val messagePair = Pair(channel, message)
            messages.add(messagePair)
            if (messagePair == lock.keyOwner) {
                lock.unlock(messagePair)
            }
        }

        override fun psubscribed(
            pattern: String,
            count: Long,
        ) {
            subscribeCallback()
        }

        suspend fun assertReceivedMessage(
            channel: String,
            message: String,
        ) {
            val messagePair = Pair(channel, message)
            if (!messages.contains(messagePair)) {
                lock.lock(messagePair)
                lock.waitForUnlock()
            }
        }
    }

    open class WithRedis {
        companion object {
            private lateinit var job: Job
            private lateinit var subscriber: RedisSubscriber
            private lateinit var redisPubSub: RedisPubSubCommands<String, String>
            val container = RedisContainer()

            const val PASSWORD = "password"

            @BeforeClass
            @JvmStatic
            fun startContainer() {
                container.start()
            }

            @AfterClass
            @JvmStatic
            fun stopContainer() {
                container.stop()
            }
        }

        @Before
        fun setupSubscriber() =
            runBlocking {
                val lock = WaitLock(true)
                subscriber = RedisSubscriber { lock.unlock() }
                val hostAndPort = redisHostAndPort()

                val redisClient =
                    RedisClient.create(
                        RedisURI
                            .builder(RedisURI.create("redis://$hostAndPort"))
                            .withAuthentication("default", PASSWORD)
                            .build(),
                    )
                val statefulRedisPubSubConnection = redisClient.connectPubSub()
                statefulRedisPubSubConnection.addListener(subscriber)
                redisPubSub = statefulRedisPubSubConnection.sync()

                job =
                    CoroutineScope(Dispatchers.IO).launch {
                        redisPubSub.psubscribe("*")
                    }
                lock.waitForUnlock()
            }

        @After
        fun closeSubscriber() {
            redisPubSub.punsubscribe()
            job.cancel()
        }

        fun redisHostAndPort(): HostAndPort = HostAndPort.of(container.host, container.getMappedPort(6379))

        fun getMessages() = subscriber.messages

        suspend fun assertReceivedMessage(
            channel: String,
            message: String,
            timeout: Duration = Duration.ofSeconds(1),
        ) {
            withTimeout(timeout.toMillis()) {
                subscriber.assertReceivedMessage(channel, message)
            }
        }
    }

    class WaitLock(
        initiallyLocked: Boolean = false,
    ) {
        private val mutex = Mutex(initiallyLocked)
        var keyOwner: Any? = null

        suspend fun lock(key: Any? = null) {
            mutex.lock(key)
            keyOwner = key
        }

        fun unlock(key: Any? = null) {
            mutex.unlock(key)
            keyOwner = null
        }

        suspend fun waitForUnlock() {
            val key = UUID.randomUUID()
            lock(key)
            unlock(key)
        }
    }
}
