package no.nav.modiacontextholder.valkey

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
import no.nav.modiacontextholder.valkey.TestUtils.WithValkey.Companion.PASSWORD
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.GenericContainer
import java.time.Duration
import java.util.*

object TestUtils {
    class ValkeyContainer : GenericContainer<ValkeyContainer>("valkey/valkey:8-alpine") {
        init {
            withCommand("valkey-server --requirepass $PASSWORD")
            withExposedPorts(6379)
        }
    }

    class ValkeySubscriber(
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

    open class WithValkey {
        companion object {
            private lateinit var job: Job
            private lateinit var subscriber: ValkeySubscriber
            private lateinit var valkeyPubSub: RedisPubSubCommands<String, String>
            val container = ValkeyContainer()

            const val PASSWORD = "password"

            @BeforeAll
            @JvmStatic
            fun startContainer() {
                container.start()
            }

            @AfterAll
            @JvmStatic
            fun stopContainer() {
                container.stop()
            }
        }

        @BeforeEach
        fun setupSubscriber() =
            runBlocking {
                val lock = WaitLock(true)
                subscriber = ValkeySubscriber { lock.unlock() }
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
                valkeyPubSub = statefulRedisPubSubConnection.sync()

                job =
                    CoroutineScope(Dispatchers.IO).launch {
                        valkeyPubSub.psubscribe("*")
                    }
                lock.waitForUnlock()
            }

        @AfterEach
        fun closeSubscriber() {
            valkeyPubSub.punsubscribe()
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
