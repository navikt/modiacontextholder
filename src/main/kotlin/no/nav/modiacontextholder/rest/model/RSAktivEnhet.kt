package no.nav.modiacontextholder.rest.model

import kotlinx.serialization.Serializable
import no.nav.modiacontextholder.domain.VeilederContext
import no.nav.modiacontextholder.domain.VeilederContextType

@Serializable
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
