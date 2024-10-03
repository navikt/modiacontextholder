package no.nav.modiacontextholder.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

class RedisTest : TestUtils.WithRedis() {
    private val hostAndPort = redisHostAndPort()
    private val redisClient =
        RedisClient
            .create(
                RedisURI
                    .builder(RedisURI.create("redis://$hostAndPort"))
                    .withAuthentication("default", PASSWORD)
                    .build(),
            )
    private val redisConnection = redisClient.connectPubSub()

    @Test
    fun `sender redis-meldinger`() =
        runBlocking {
            val redisPublisher = RedisPublisher(redisConnection, "TestChannel")
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
            val redisSubscription = Redis.Consumer(redisClient, "TestChannel")
            redisSubscription.start()
            launch {
                redisSubscription.getFlow().filterNotNull().collect {
                    defferedMessage.complete(it)
                    cancel()
                }
            }

            val redisPublisher = RedisPublisher(redisConnection, "TestChannel")
            println("Awaiting")
            val job =
                launch {
                    assertEquals(defferedMessage.await(), "TestMessage")
                }
            while (job.isActive) {
                redisPublisher.publishMessage("TestMessage")
                delay(1000)
            }
            redisConnection.close()
        }
}
