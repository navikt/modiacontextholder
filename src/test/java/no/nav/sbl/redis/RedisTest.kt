package no.nav.sbl.redis

import kotlinx.coroutines.runBlocking
import no.nav.sbl.redis.TestUtils.WithRedis.Companion.PASSWORD
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue

class RedisTest : TestUtils.WithRedis() {
    @Test
    fun `sender redis-meldinger`() = runBlocking {
        val hostAndPort = redisHostAndPort()
        val authJedisPool = AuthJedisPool(
            redisHostPortAndPassword = RedisHostPortAndPassword(
                host = hostAndPort.host,
                port = hostAndPort.port,
                password = PASSWORD,
            ),
        )
        val redisPublisher = RedisPublisher(authJedisPool, "TestChannel")
        redisPublisher.publishMessage("TestMessage1")
        redisPublisher.publishMessage("TestMessage2")
        redisPublisher.publishMessage("TestMessage3")
        assertReceivedMessage("TestChannel", "TestMessage3")
        assertReceivedMessage("TestChannel", "TestMessage2")
        assertReceivedMessage("TestChannel", "TestMessage1")
        assertTrue(getMessages().size == 3)
    }
}
