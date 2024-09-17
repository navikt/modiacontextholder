package no.nav.modiacontextholder.utils

import io.ktor.server.plugins.BadRequestException
import io.ktor.server.websocket.*
import io.ktor.websocket.Frame
import io.micrometer.core.instrument.Gauge
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import no.nav.modiacontextholder.log
import no.nav.modiacontextholder.metricsRegistry
import no.nav.modiacontextholder.rest.model.RSEvent
import java.time.Duration
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap

class WebsocketStorage(
    private val flow: Flow<String?>,
) {
    companion object {
        val options: WebSockets.WebSocketOptions.() -> Unit = {
            pingPeriod = Duration.ofMinutes(3)
        }
    }

    init {
        Gauge
            .builder("websocket_clients", this::getAntallTilkoblinger)
            .register(metricsRegistry)
        GlobalScope.launch { propagateMessageToWebsocket() }
    }

    private val sessions = ConcurrentHashMap<String, MutableList<WebSocketServerSession>>()
    val wsHandler: suspend DefaultWebSocketServerSession.() -> Unit = {
        val ident = (call.parameters["ident"] ?: throw BadRequestException("No ident found"))
        log.info("Connected to websocket from $ident")
        try {
            sessions.computeIfAbsent(ident) { mutableListOf() }.add(this)
            incoming.receive() // Waiting so that the connection isn't closed at once
        } catch (e: ClosedReceiveChannelException) {
            // This is expected when the channel is closed, `finally`-block will remove the session
        } catch (e: Throwable) {
            log.error("Websocket error", e)
        } finally {
            sessions[ident]?.remove(this)
        }
    }

    private fun getAntallTilkoblinger(): Int =
        sessions
            .values
            .map { it.size }
            .sum()

    private suspend fun propagateMessageToWebsocket() {
        flow.filterNotNull().collect { value ->
            try {
                val event = value.fromJson<RSEvent>()
                val (veilederIdent, eventType) = event
                log.info("Sending $eventType to $veilederIdent")

                val frame = Frame.Text(eventType)
                sessions[veilederIdent]?.forEach {
                    it.send(frame)
                }
            } catch (_: CancellationException) {
                // Ignore these types of errors
            } catch (e: Exception) {
                log.error("Error propagating message to Websocket:", e)
            }
        }
    }
}

inline fun <reified T> String.fromJson(): T = Json.decodeFromString<T>(this)
