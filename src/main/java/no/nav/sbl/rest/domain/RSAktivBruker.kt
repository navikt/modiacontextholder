package no.nav.sbl.rest.domain

import no.nav.sbl.db.domain.EventType
import no.nav.sbl.db.domain.PEvent

data class RSAktivBruker(
    val aktivBruker: String?,
) {
    companion object {
        fun from(pEvent: PEvent): RSAktivBruker =
            RSAktivBruker(
                if (EventType.NY_AKTIV_BRUKER.name == pEvent.eventType) pEvent.verdi else null,
            )
    }
}
