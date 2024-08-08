package no.nav.sbl.config

import no.nav.sbl.websocket.ContextWebSocketHandler
import no.nav.sbl.websocket.WebSocketRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket

@Configuration
@EnableWebSocket
open class WebSocketConfiguration {
    @Bean
    open fun webSocketRegistry(contextWebSocketHandler: ContextWebSocketHandler): WebSocketRegistry =
        WebSocketRegistry(contextWebSocketHandler)

    @Bean
    open fun webSocketHandler(): ContextWebSocketHandler = ContextWebSocketHandler()
}
