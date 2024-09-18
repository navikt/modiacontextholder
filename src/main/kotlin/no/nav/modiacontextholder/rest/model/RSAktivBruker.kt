package no.nav.modiacontextholder.rest.model

import kotlinx.serialization.Serializable
import no.nav.modiacontextholder.domain.VeilederContext
import no.nav.modiacontextholder.domain.VeilederContextType

@Serializable
data class RSAktivBruker(
    val aktivBruker: String?,
) {
    companion object {
        fun from(veilederContext: VeilederContext): RSAktivBruker =
            RSAktivBruker(
                if (VeilederContextType.NY_AKTIV_BRUKER == veilederContext.contextType) veilederContext.verdi else null,
            )
    }
}
