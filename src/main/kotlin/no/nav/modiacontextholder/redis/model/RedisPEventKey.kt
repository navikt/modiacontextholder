package no.nav.modiacontextholder.redis.model

data class RedisPEventKey(
    val contextType: RedisVeilederContextType,
    val veilederIdent: String,
) {
    override fun toString(): String = "veiledercontext:${contextType.name}:$veilederIdent"
}
