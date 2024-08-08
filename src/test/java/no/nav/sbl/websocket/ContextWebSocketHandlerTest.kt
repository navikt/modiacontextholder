package no.nav.sbl.websocket

import kotlinx.coroutines.CompletableDeferred
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
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
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
            val deferredMessage = CompletableDeferred<String>()
            val webSocketClient = StandardWebSocketClient()

            val clientWebSocketHandler =
                ClientWebSocketHandler(deferredMessage)

            val session =
                webSocketClient
                    .execute(
                        clientWebSocketHandler,
                        "ws://localhost:$port/ws/$ident",
                    ).get()

            handler.publishMessage(ident, "eventType")
            val responseMessage = deferredMessage.await()

            session.close()

            assertThat(responseMessage).isEqualTo("eventType")
        }

    @Test
    fun `client can have multiple sessions at the same time`() =
        runBlocking<Unit> {
            val ident = "ident2"
            val defferedMessage1 = CompletableDeferred<String>()
            val defferedMessage2 = CompletableDeferred<String>()

            val webSocketClient = StandardWebSocketClient()

            val session1 =
                webSocketClient
                    .execute(
                        ClientWebSocketHandler(defferedMessage1),
                        "ws://localhost:$port/ws/$ident",
                    ).get(5, TimeUnit.SECONDS)

            val session2 =
                webSocketClient
                    .execute(ClientWebSocketHandler(defferedMessage2), "ws://localhost:$port/ws/$ident")
                    .get(1, TimeUnit.SECONDS)

            handler.publishMessage(ident, "eventType")
            val responseMessage1 = defferedMessage1.await()
            val responseMessage2 = defferedMessage2.await()

            session1.close()
            session2.close()

            assertThat(responseMessage1).isEqualTo("eventType")
            assertThat(responseMessage2).isEqualTo("eventType")
        }

    class ClientWebSocketHandler(
        private val deferredMessage: CompletableDeferred<String>,
    ) : TextWebSocketHandler() {
        override fun handleTextMessage(
            session: WebSocketSession,
            message: TextMessage,
        ) {
            deferredMessage.complete(message.payload)
        }
    }
}
