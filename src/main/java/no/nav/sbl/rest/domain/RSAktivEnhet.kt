package no.nav.sbl.rest.domain

import no.nav.sbl.db.domain.EventType
import no.nav.sbl.db.domain.PEvent

data class RSAktivEnhet(
    val aktivEnhet: String?
) {
    companion object {
        fun from(pEvent: PEvent): RSAktivEnhet {
            val aktivEnhet = if (pEvent.eventType == EventType.NY_AKTIV_ENHET.name) {
                pEvent.verdi
            } else {
                null
            }

            return RSAktivEnhet(aktivEnhet)
        }
    }
}
