package no.nav.sbl.rest.model

import no.nav.sbl.domain.ContextEvent

data class RSEvent(
    val veilederIdent: String,
    val eventType: String,
) {
    companion object {
        fun from(contextEvent: ContextEvent) =
            RSEvent(
                veilederIdent = contextEvent.veilederIdent,
                eventType = contextEvent.eventType.name,
            )
    }
}