package no.nav.sbl.websocket

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

class ClientWebSocketHandler(
    private val channel: Channel<String>,
) : TextWebSocketHandler() {
    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage,
    ) {
        runBlocking {
            channel.send(message.payload)
        }
    }
}