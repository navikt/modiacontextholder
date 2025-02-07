package no.nav.modiacontextholder.valkey

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

class ValkeyTest : TestUtils.WithValkey() {
    private val hostAndPort = redisHostAndPort()
    private val valkeyClient =
        RedisClient
            .create(
                RedisURI
                    .builder(RedisURI.create("redis://$hostAndPort"))
                    .withAuthentication("default", PASSWORD)
                    .build(),
            )
    private val valkeyConnection = valkeyClient.connectPubSub()

    @Test
    fun `sender valkey-meldinger`() =
        runBlocking {
            val redisPublisher = ValkeyPublisher(valkeyConnection, "TestChannel")
            redisPublisher.publishMessage("TestMessage1")
            redisPublisher.publishMessage("TestMessage2")
            redisPublisher.publishMessage("TestMessage3")
            assertReceivedMessage("TestChannel", "TestMessage3")
            assertReceivedMessage("TestChannel", "TestMessage2")
            assertReceivedMessage("TestChannel", "TestMessage1")
            assertTrue(getMessages().size == 3)
        }

    @Test
    fun `valkey subscriber mottar meldinger`() =
        runBlocking {
            val defferedMessage = CompletableDeferred<String>()
            val valkeySubscription = Valkey.Consumer(valkeyClient, "TestChannel")
            valkeySubscription.start()
            launch {
                valkeySubscription.getFlow().filterNotNull().collect {
                    defferedMessage.complete(it)
                    cancel()
                }
            }

            val valkeyPublisher = ValkeyPublisher(valkeyConnection, "TestChannel")
            println("Awaiting")
            val job =
                launch {
                    assertEquals(defferedMessage.await(), "TestMessage")
                }
            while (job.isActive) {
                valkeyPublisher.publishMessage("TestMessage")
                delay(1000)
            }
            valkeyConnection.close()
        }
}
