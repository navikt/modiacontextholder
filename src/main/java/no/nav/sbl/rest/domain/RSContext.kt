package no.nav.sbl.rest.domain

import no.nav.sbl.domain.ContextEvent
import no.nav.sbl.domain.ContextEventType

data class RSContext(
    var aktivBruker: String? = null,
    var aktivEnhet: String? = null,
) {
    companion object {
        fun from(contextEvent: ContextEvent): RSContext =
            RSContext(
                aktivBruker = if (ContextEventType.NY_AKTIV_BRUKER == contextEvent.eventType) contextEvent.verdi else null,
                aktivEnhet = if (ContextEventType.NY_AKTIV_ENHET == contextEvent.eventType) contextEvent.verdi else null,
            )
    }
}
