package no.nav.sbl.config

import no.nav.sbl.websocket.ContextWebSocketHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
open class WebSocketConfiguration(
    private val webSocketHandler: WebSocketHandler,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(webSocketHandler, "/ws/{ident}")
            .setAllowedOrigins("*")
    }

    @Bean
    open fun webSocketHandler(): ContextWebSocketHandler = ContextWebSocketHandler()
}
