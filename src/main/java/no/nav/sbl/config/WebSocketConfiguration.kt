package no.nav.sbl.config

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
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
    open fun webSocketHandler(meterRegistry: MeterRegistry): ContextWebSocketHandler =
        ContextWebSocketHandler().also {
            Gauge
                .builder("websocket_clients", it::activeSessions)
                .description("Antall aktive websocket-sessioner")
                .register(meterRegistry)
        }
}
