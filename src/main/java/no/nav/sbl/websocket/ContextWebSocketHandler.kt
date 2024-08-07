package no.nav.sbl.websocket

import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class ContextWebSocketHandler : TextWebSocketHandler() {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val sessions = ConcurrentHashMap<String, MutableList<WebSocketSession>>()

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
        try {
            sessions[ident]?.forEach {
                it.sendMessage(TextMessage(message))
            }
        } catch (e: IOException) {
            log.error("Error sending message to websocket", e)
        }
    }
}
