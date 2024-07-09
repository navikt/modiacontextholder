package no.nav.sbl.rest.domain

import no.nav.sbl.domain.ContextEventType
import no.nav.sbl.domain.ContextEvent

data class RSAktivEnhet(
    val aktivEnhet: String?
) {
    companion object {
        fun from(contextEvent: ContextEvent): RSAktivEnhet {
            val aktivEnhet = if (contextEvent.eventType == ContextEventType.NY_AKTIV_ENHET.name) {
                contextEvent.verdi
            } else {
                null
            }

            return RSAktivEnhet(aktivEnhet)
        }
    }
}
