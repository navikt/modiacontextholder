package no.nav.modiacontextholder

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.lettuce.core.RedisClient
import no.nav.modiacontextholder.redis.setupRedisConsumer
import no.nav.modiacontextholder.utils.WebsocketStorage
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.minutes

fun Application.setupWebsocket() {
    val redisClient: RedisClient by inject()
    val redisConsumer = setupRedisConsumer(redisClient)
    val websocketStorage = WebsocketStorage(redisConsumer.getFlow())

    install(WebSockets) {
        pingPeriod = 1.minutes
        timeout = 5.minutes
    }

    routing {
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
