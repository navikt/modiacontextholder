package no.nav.sbl.redis

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import no.nav.sbl.redis.TestUtils.WithRedis.Companion.PASSWORD
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.testcontainers.containers.GenericContainer
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import java.time.Duration
import java.util.*

object TestUtils {

    class RedisContainer : GenericContainer<RedisContainer>("redis:6-alpine") {
        init {
            withCommand("redis-server --requirepass $PASSWORD")
            withExposedPorts(6379)
        }
    }

    class RedisSubscriber(private val subscribeCallback: () -> Unit = {}) : JedisPubSub() {
        val messages: MutableList<Pair<String, String>> = mutableListOf()
        private val lock = WaitLock(false)

        override fun onPMessage(pattern: String, channel: String, message: String) {
            val messagePair = Pair(channel, message)
            messages.add(messagePair)
            if (messagePair == lock.keyOwner) {
                lock.unlock(messagePair)
            }
        }

        override fun onPSubscribe(pattern: String?, subscribedChannels: Int) {
            subscribeCallback()
        }

        suspend fun assertReceivedMessage(channel: String, message: String) {
            val messagePair = Pair(channel, message)
            if (!messages.contains(messagePair)) {
                lock.lock(messagePair)
                lock.waitForUnlock()
            }
        }
    }

    open class WithRedis {

        companion object {
            private var job: Job? = null
            private var subscriber: RedisSubscriber? = null
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
        fun setupSubscriber() = runBlocking {
            val lock = WaitLock(true)
            subscriber = RedisSubscriber { lock.unlock() }
            val hostAndPort = redisHostAndPort()
            val jedis = Jedis(hostAndPort)
            jedis.auth(PASSWORD)
            job = GlobalScope.launch {
                jedis.psubscribe(subscriber, "*")
            }
            lock.waitForUnlock()
        }

        @After
        fun closeSubscriber() {
            subscriber?.unsubscribe()
            job?.cancel()
            subscriber = null
            job = null
        }

        fun redisHostAndPort() = HostAndPort(container.host, container.getMappedPort(6379))

        fun getMessages() = subscriber?.messages ?: emptyList<Pair<String, String>>()

        suspend fun assertReceivedMessage(channel: String, message: String, timeout: Duration = Duration.ofSeconds(1)) {
            withTimeout(timeout.toMillis()) {
                subscriber?.assertReceivedMessage(channel, message)
            }
        }
    }

    class WaitLock(initiallyLocked: Boolean = false) {
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
