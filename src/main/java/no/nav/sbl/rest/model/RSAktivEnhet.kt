package no.nav.sbl.rest.model

import no.nav.sbl.domain.VeilederContext
import no.nav.sbl.domain.VeilederContextType

data class RSAktivEnhet(
    val aktivEnhet: String?,
) {
    companion object {
        fun from(veilederContext: VeilederContext): RSAktivEnhet {
            val aktivEnhet =
                if (veilederContext.contextType == VeilederContextType.NY_AKTIV_ENHET) {
                    veilederContext.verdi
                } else {
                    null
                }

            return RSAktivEnhet(aktivEnhet)
        }
    }
}
