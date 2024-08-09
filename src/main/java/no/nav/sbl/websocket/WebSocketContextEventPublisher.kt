package no.nav.sbl.websocket

import org.springframework.stereotype.Component

@Component
class WebSocketContextEventPublisher(
    private val contextWebSocketHandler: ContextWebSocketHandler,
) {
    fun publishMessage(
        ident: String,
        eventType: String,
    ) {
        contextWebSocketHandler.publishMessage(ident, eventType)
    }
}
