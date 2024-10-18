package no.nav.modiacontextholder

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.lettuce.core.RedisClient
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import no.nav.modiacontextholder.redis.setupRedisConsumer
import no.nav.modiacontextholder.utils.WebsocketStorage
import org.koin.ktor.ext.inject
import java.time.Duration

fun Application.setupWebsocket() {
    val redisClient: RedisClient by inject()
    val redisConsumer = setupRedisConsumer(redisClient)

    @OptIn(DelicateCoroutinesApi::class)
    val websocketStorage = WebsocketStorage(redisConsumer.getFlow(), GlobalScope)

    install(WebSockets) {
        pingPeriod = Duration.ofMinutes(1)
        timeout = Duration.ofMinutes(5)
    }

    routing {
        /**
         * Websocket for context updates
         *
         * @OpenAPITag websocket
         */
        webSocket(path = "/ws/{ident}", handler = websocketStorage.wsHandler)
    }

    redisConsumer.start()

    Runtime.getRuntime().addShutdownHook(
        Thread {
            log.info("[WS module] Shutdown hook called: Shutting down redisconsumer")
            redisConsumer.stop()
        },
    )
}
