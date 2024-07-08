package no.nav.sbl.redis.model

import no.nav.sbl.db.domain.PEvent
import java.time.LocalDateTime

data class RedisPEvent(
    val veilederIdent: String,
    val eventType: RedisEventType,
    val verdi: String,
    val created: LocalDateTime,
) {
    val key: RedisPEventKey = RedisPEventKey(eventType, veilederIdent)

    fun toPEvent(): PEvent =
        PEvent(
            veilederIdent = veilederIdent,
            eventType = eventType.toDomainEventType(),
            verdi = verdi,
            created = created,
        )

    companion object {
        fun from(pEvent: PEvent): RedisPEvent =
            RedisPEvent(
                veilederIdent = pEvent.veilederIdent!!,
                eventType = RedisEventType.from(pEvent.eventType!!),
                verdi = pEvent.verdi!!,
                created = pEvent.created ?: LocalDateTime.now(),
            )
    }
}
