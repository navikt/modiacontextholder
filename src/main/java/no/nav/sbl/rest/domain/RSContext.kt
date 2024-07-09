package no.nav.sbl.rest.domain

import no.nav.sbl.domain.ContextEventType
import no.nav.sbl.domain.ContextEvent

data class RSContext(
    var aktivBruker: String? = null,
    var aktivEnhet: String? = null,
) {
    companion object {
        fun from(contextEvent: ContextEvent): RSContext =
            RSContext(
                aktivBruker = if (ContextEventType.NY_AKTIV_BRUKER.name == contextEvent.eventType) contextEvent.verdi else null,
                aktivEnhet = if (ContextEventType.NY_AKTIV_ENHET.name == contextEvent.eventType) contextEvent.verdi else null,
            )
    }
}
