package no.nav.sbl.rest.domain

import no.nav.sbl.db.domain.EventType
import no.nav.sbl.db.domain.PEvent

data class RSContext(
    var aktivBruker: String? = null,
    var aktivEnhet: String? = null,
) {
    companion object {
        fun from(pEvent: PEvent): RSContext =
            RSContext(
                aktivBruker = if (EventType.NY_AKTIV_BRUKER.name == pEvent.eventType) pEvent.verdi else null,
                aktivEnhet = if (EventType.NY_AKTIV_ENHET.name == pEvent.eventType) pEvent.verdi else null,
            )
    }
}
