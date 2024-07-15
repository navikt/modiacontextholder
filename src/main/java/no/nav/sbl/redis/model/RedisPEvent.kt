package no.nav.sbl.redis.model

import no.nav.sbl.domain.VeilederContext
import java.time.LocalDateTime

data class RedisPEvent(
    val veilederIdent: String,
    val contextType: RedisVeilederContextType,
    val verdi: String,
    val created: LocalDateTime,
) {
    val key: RedisPEventKey = RedisPEventKey(contextType, veilederIdent)

    fun toPEvent(): VeilederContext =
        VeilederContext(
            veilederIdent = veilederIdent,
            contextType = contextType.toDomain(),
            verdi = verdi,
            created = created,
        )

    companion object {
        fun from(veilederContext: VeilederContext): RedisPEvent =
            RedisPEvent(
                veilederIdent = veilederContext.veilederIdent,
                contextType = RedisVeilederContextType.from(veilederContext.contextType),
                verdi = veilederContext.verdi,
                created = veilederContext.created ?: LocalDateTime.now(),
            )
    }
}
