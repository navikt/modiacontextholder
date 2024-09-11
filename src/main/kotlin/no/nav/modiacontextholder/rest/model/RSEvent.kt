package no.nav.modiacontextholder.rest.model

import no.nav.modiacontextholder.domain.VeilederContext

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
