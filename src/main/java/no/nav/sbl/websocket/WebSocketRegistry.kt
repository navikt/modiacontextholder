package no.nav.sbl.websocket

import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

open class WebSocketRegistry(
    private val contextWebSocketHandler: ContextWebSocketHandler,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(contextWebSocketHandler, "/ws/{ident}").setAllowedOrigins("*")
    }
}