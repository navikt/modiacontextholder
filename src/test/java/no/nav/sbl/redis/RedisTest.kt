package no.nav.sbl.redis

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import no.nav.common.health.HealthCheckResult
import org.junit.*
import org.junit.Assert.*
import org.testcontainers.containers.GenericContainer
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub

object TestUtils {
    class RedisContainer : GenericContainer<RedisContainer>("redis:6-alpine")
    
    class RedisSubscriber : JedisPubSub() {
        val messages: MutableList<Pair<String, String>> = mutableListOf()
        
        override fun onPMessage(pattern: String, channel: String, message: String) {
            messages.add(Pair(channel, message))
        }
    }
    
    interface WithRedis {
        
        companion object {
            private var job: Job? = null
            private var subscriber: RedisSubscriber? = null
            private val container = RedisContainer()
            
            
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
        fun setupSubscriber() {
            subscriber = RedisSubscriber()
            val jedis = Jedis(redisHostAndPort())
            job = GlobalScope.launch {
                jedis.psubscribe(subscriber, "*")
            }
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
    }
}

class RedisTest : TestUtils.WithRedis {
    
    private val redisPublisher = Redis.Publisher(redisHostAndPort())
    
    @Test
    fun `sender redis-melding`() {
        redisPublisher.publishMessage("Test", "Test")
        assertEquals("Test", getMessages()[0])
    }
    
    @Test
    fun `redis selfcheck ok`() {
        assertEquals(redisPublisher.checkHealth(), HealthCheckResult.healthy())
    }
}