package no.nav.sbl.rest.model

import no.nav.sbl.domain.ContextEvent
import no.nav.sbl.domain.ContextEventType

data class RSAktivBruker(
    val aktivBruker: String?,
) {
    companion object {
        fun from(contextEvent: ContextEvent): RSAktivBruker =
            RSAktivBruker(
                if (ContextEventType.NY_AKTIV_BRUKER == contextEvent.eventType) contextEvent.verdi else null,
            )
    }
}
