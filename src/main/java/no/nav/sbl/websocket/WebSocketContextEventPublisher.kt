package no.nav.sbl.websocket

import no.nav.sbl.service.ContextEventPublisher
import org.springframework.stereotype.Component

@Component
class WebSocketContextEventPublisher(
    private val contextWebSocketHandler: ContextWebSocketHandler,
) : ContextEventPublisher {
    override fun publishMessage(
        ident: String,
        eventType: String,
    ) {
        contextWebSocketHandler.publishMessage(ident, eventType)
    }
}
