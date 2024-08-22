package no.nav.sbl.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue

class RedisTest : TestUtils.WithRedis() {
    private val hostAndPort = redisHostAndPort()
    private val redisConnection =
        RedisClient
            .create(
                RedisURI
                    .builder(RedisURI.create("redis://$hostAndPort"))
                    .withAuthentication("default", PASSWORD)
                    .build(),
            ).connectPubSub()
    private val redis = redisConnection.sync()

    @Test
    fun `sender redis-meldinger`() =
        runBlocking {
            val redisPublisher = RedisPublisher(redis, "TestChannel")
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
                    println("Received message $message")
                    defferedMessage.complete(message)
                }
            redisConnection.addListener(redisSubscription)
            redis.subscribe(redisSubscription.channel)
            val redisPublisher = RedisPublisher(redis, "TestChannel")
            println("Awaiting")
            val job =
                launch {
                    assertThat(defferedMessage.await()).isEqualTo("TestMessage")
                }
            while (job.isActive) {
                redisPublisher.publishMessage("TestMessage")
                delay(1000)
            }
            redis.unsubscribe(redisSubscription.channel)
            redisConnection.close()
        }
}
