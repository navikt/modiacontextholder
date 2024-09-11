package no.nav.modiacontextholder.rest.model

import no.nav.modiacontextholder.domain.VeilederContext
import no.nav.modiacontextholder.domain.VeilederContextType

data class RSContext(
    var aktivBruker: String? = null,
    var aktivEnhet: String? = null,
) {
    companion object {
        fun from(veilederContext: VeilederContext): RSContext =
            RSContext(
                aktivBruker = if (VeilederContextType.NY_AKTIV_BRUKER == veilederContext.contextType) veilederContext.verdi else null,
                aktivEnhet = if (VeilederContextType.NY_AKTIV_ENHET == veilederContext.contextType) veilederContext.verdi else null,
            )
    }
}
