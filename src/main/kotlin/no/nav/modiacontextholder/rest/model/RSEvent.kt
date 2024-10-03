package no.nav.modiacontextholder.rest.model

import kotlinx.serialization.Serializable
import no.nav.modiacontextholder.domain.VeilederContext

@Serializable
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
