package no.nav.modiacontextholder.redis.model

import kotlinx.serialization.Serializable

@Serializable
data class RedisPEventKey(
    val contextType: RedisVeilederContextType,
    val veilederIdent: String,
) {
    override fun toString(): String = "veiledercontext:${contextType.name}:$veilederIdent"
}
