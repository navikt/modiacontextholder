package no.nav.sbl.redis.model

data class RedisPEventKey(
    val eventType: RedisEventType,
    val veilederIdent: String,
) {
    override fun toString(): String = "veiledercontext:${eventType.name}:$veilederIdent"
}