package no.nav.sbl.websocket

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import no.nav.sbl.config.WebSocketConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.util.concurrent.TimeUnit

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = [WebSocketConfiguration::class])
@EnableAutoConfiguration
class ContextWebSocketHandlerTest {
    @LocalServerPort
    var port: Int = 0

    @Autowired
    private lateinit var handler: ContextWebSocketHandler

    @Test
    fun `client can connect to websocket and receive message`() =
        runBlocking<Unit> {
            val ident = "ident1"
            val channel = Channel<String>()
            val webSocketClient = StandardWebSocketClient()

            val clientWebSocketHandler =
                ClientWebSocketHandler(channel)

            val session =
                webSocketClient
                    .execute(
                        clientWebSocketHandler,
                        "ws://localhost:$port/ws/$ident",
                    ).get()

            handler.publishMessage(ident, "eventType")
            val responseMessage = channel.receive()

            session.close()

            assertThat(responseMessage).isEqualTo("eventType")
        }

    @Test
    fun `client can have multiple sessions at the same time`() =
        runBlocking<Unit> {
            val ident = "ident2"
            val channel = Channel<String>()

            val webSocketClient = StandardWebSocketClient()

            val session1 =
                webSocketClient
                    .execute(
                        ClientWebSocketHandler(channel),
                        "ws://localhost:$port/ws/$ident",
                    ).get(5, TimeUnit.SECONDS)

            val session2 =
                webSocketClient
                    .execute(ClientWebSocketHandler(channel), "ws://localhost:$port/ws/$ident")
                    .get(1, TimeUnit.SECONDS)

            handler.publishMessage(ident, "eventType")
            val responseMessage1 = channel.receive()
            val responseMessage2 = channel.receive()

            session1.close()
            session2.close()

            assertThat(responseMessage1).isEqualTo("eventType")
            assertThat(responseMessage2).isEqualTo("eventType")
        }

}
