package no.nav.sbl.redis

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.JedisPooled

class RedisTest : TestUtils.WithRedis() {
    private val hostAndPort = redisHostAndPort()
    private val jedisClientConfig =
        DefaultJedisClientConfig
            .builder()
            .user("default")
            .password(PASSWORD)
            .build()
    private val jedisPooled = JedisPooled(hostAndPort, jedisClientConfig)

    @Test
    fun `sender redis-meldinger`() =
        runBlocking {
            val redisPublisher = RedisPublisher(jedisPooled, "TestChannel")
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
            val redisSubscriber = RedisSubscriber(jedisPooled, listOf(redisSubscription))
            redisSubscriber.subscribe()
            val redisPublisher = RedisPublisher(jedisPooled, "TestChannel")
            println("Awaiting")
            val job =
                launch {
                    assertThat(defferedMessage.await()).isEqualTo("TestMessage")
                }
            while (job.isActive) {
                redisPublisher.publishMessage("TestMessage")
                delay(1000)
            }
            redisSubscriber.unsubscribe()
        }
}
