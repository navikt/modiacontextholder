package no.nav.sbl.rest.model

import no.nav.sbl.domain.VeilederContext
import no.nav.sbl.domain.VeilederContextType

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
