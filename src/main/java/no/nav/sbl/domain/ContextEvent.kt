package no.nav.sbl.domain

import java.time.LocalDateTime

data class ContextEvent(
    val veilederIdent: String,
    val eventType: ContextEventType,
    val created: LocalDateTime? = null,
    val verdi: String,
)