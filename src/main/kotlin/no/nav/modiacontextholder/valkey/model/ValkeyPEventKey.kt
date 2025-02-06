package no.nav.modiacontextholder.valkey.model

import kotlinx.serialization.Serializable

@Serializable
data class ValkeyPEventKey(
    val contextType: ValkeyVeilederContextType,
    val veilederIdent: String,
) {
    override fun toString(): String = "veiledercontext:${contextType.name}:$veilederIdent"
}
