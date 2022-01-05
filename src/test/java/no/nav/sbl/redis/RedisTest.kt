package no.nav.sbl.redis

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue

class RedisTest : TestUtils.WithRedis {
    private val redisPublisher = Redis.Publisher(redisHostAndPort())
    
    @Test
    fun `sender redis-meldinger`() = runBlocking {
        redisPublisher.publishMessage("TestChannel", "TestMessage1")
        redisPublisher.publishMessage("TestChannel", "TestMessage2")
        redisPublisher.publishMessage("TestChannel", "TestMessage3")
        assertReceivedMessage("TestChannel", "TestMessage3")
        assertReceivedMessage("TestChannel", "TestMessage2")
        assertReceivedMessage("TestChannel", "TestMessage1")
        assertTrue(getMessages().size == 3)
    }
    
    @Test
    fun `redis selfcheck ok`() {
        assertTrue(redisPublisher.checkHealth().isHealthy)
    }
}