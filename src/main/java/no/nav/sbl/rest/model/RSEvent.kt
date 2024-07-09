package no.nav.sbl.rest.model

import no.nav.sbl.domain.VeilederContext

data class RSEvent(
    val veilederIdent: String,
    val eventType: String,
) {
    companion object {
        fun from(veilederContext: VeilederContext) =
            RSEvent(
                veilederIdent = veilederContext.veilederIdent,
                eventType = veilederContext.contextType.name,
            )
    }
}
