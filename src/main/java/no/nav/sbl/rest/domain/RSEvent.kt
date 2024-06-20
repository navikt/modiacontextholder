package no.nav.sbl.rest.domain;

import no.nav.sbl.db.domain.PEvent

data class RSEvent(
    val veilederIdent: String,
    val eventType: String,
) {
    companion object {
        fun from(pEvent: PEvent) = RSEvent(
            veilederIdent = pEvent.veilederIdent!!,
            eventType = pEvent.eventType!!,
        )
    }
}
