package no.nav.sbl.redis

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue

class RedisTest : TestUtils.WithRedis() {
    private val hostAndPort = redisHostAndPort()
    private val authJedisPool =
        AuthJedisPool(
            uriWithAuth =
                RedisUriWithAuth(
                    uri = "redis://${hostAndPort.host}:${hostAndPort.port}",
                    user = "default",
                    password = PASSWORD,
                ),
        )

    @Test
    fun `sender redis-meldinger`() =
        runBlocking {
            val redisPublisher = RedisPublisher(authJedisPool, "TestChannel")
            redisPublisher.publishMessage("TestMessage1")
            redisPublisher.publishMessage("TestMessage2")
            redisPublisher.publishMessage("TestMessage3")
            assertReceivedMessage("TestChannel", "TestMessage3")
            assertReceivedMessage("TestChannel", "TestMessage2")
            assertReceivedMessage("TestChannel", "TestMessage1")
            assertTrue(getMessages().size == 3)
        }

    @Test
    fun `redis subscriber mottar meldinger`() =
        runBlocking {
            val defferedMessage = CompletableDeferred<String>()
            val redisSubscription =
                RedisSubscription("TestChannel") { _, message ->
                    defferedMessage.complete(message)
                }
            val redisSubscriber = RedisSubscriber(authJedisPool, listOf(redisSubscription))
            redisSubscriber.start()
            val redisPublisher = RedisPublisher(authJedisPool, "TestChannel")
            redisPublisher.publishMessage("TestMessage")
            assertThat(defferedMessage.await()).isEqualTo("TestMessage")
            redisSubscriber.stop()
        }
}
