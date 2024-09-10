package no.nav.sbl.websocket

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.PingMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

@EnableScheduling
class ContextWebSocketHandler : TextWebSocketHandler() {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val sessions = ConcurrentHashMap<String, MutableList<WebSocketSession>>()

    val activeSessions: Int get() = sessions.values.sumOf { it.size }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val ident =
            session.uri
                ?.path
                ?.split("/")
                ?.lastOrNull()
        if (ident != null) {
            sessions.computeIfAbsent(ident) { mutableListOf() }.add(session)
        }
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        val ident =
            session.uri
                ?.path
                ?.split("/")
                ?.lastOrNull()
        if (ident != null) {
            sessions[ident]?.remove(session)
        }
    }

    fun publishMessage(
        ident: String,
        message: String,
    ) {
        runCatching {
            sessions[ident]?.forEach {
                it.sendMessage(TextMessage(message))
            }
        }.onFailure {
            when (it) {
                is IOException -> log.error("Error sending message to websocket", it)
                is IllegalStateException, is NullPointerException -> log.debug("Session is closed, ignoring message")
            }
        }
    }

    @Scheduled(fixedRate = 30 * 1000)
    fun heartbeat() {
        val ping = PingMessage(ByteBuffer.allocate(1))
        sessions.values.forEach {
            it.forEach { session ->
                runCatching {
                    session.sendMessage(ping)
                }.onFailure { err ->
                    when (err) {
                        is IOException -> log.error("Error sending PING to websocket", err)
                        else -> log.error("Error sending to websocket", err)
                    }
                }
            }
        }
    }
}
