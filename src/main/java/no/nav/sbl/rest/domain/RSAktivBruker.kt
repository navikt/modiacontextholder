package no.nav.sbl.rest.domain

import no.nav.sbl.domain.ContextEventType
import no.nav.sbl.domain.ContextEvent

data class RSAktivBruker(
    val aktivBruker: String?,
) {
    companion object {
        fun from(contextEvent: ContextEvent): RSAktivBruker =
            RSAktivBruker(
                if (ContextEventType.NY_AKTIV_BRUKER.name == contextEvent.eventType) contextEvent.verdi else null,
            )
    }
}
