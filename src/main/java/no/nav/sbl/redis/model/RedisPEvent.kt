package no.nav.sbl.redis.model

import no.nav.sbl.domain.VeilederContext
import java.time.LocalDateTime

data class RedisPEvent(
    val veilederIdent: String,
    val eventType: RedisEventType,
    val verdi: String,
    val created: LocalDateTime,
) {
    val key: RedisPEventKey = RedisPEventKey(eventType, veilederIdent)

    fun toPEvent(): VeilederContext =
        VeilederContext(
            veilederIdent = veilederIdent,
            contextType = eventType.toDomainEventType(),
            verdi = verdi,
            created = created,
        )

    companion object {
        fun from(veilederContext: VeilederContext): RedisPEvent =
            RedisPEvent(
                veilederIdent = veilederContext.veilederIdent,
                eventType = RedisEventType.from(veilederContext.contextType),
                verdi = veilederContext.verdi,
                created = veilederContext.created ?: LocalDateTime.now(),
            )
    }
}
