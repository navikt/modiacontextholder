package no.nav.sbl.redis.model

import no.nav.sbl.domain.ContextEvent
import java.time.LocalDateTime

data class RedisPEvent(
    val veilederIdent: String,
    val eventType: RedisEventType,
    val verdi: String,
    val created: LocalDateTime,
) {
    val key: RedisPEventKey = RedisPEventKey(eventType, veilederIdent)

    fun toPEvent(): ContextEvent =
        ContextEvent(
            veilederIdent = veilederIdent,
            eventType = eventType.toDomainEventType(),
            verdi = verdi,
            created = created,
        )

    companion object {
        fun from(contextEvent: ContextEvent): RedisPEvent =
            RedisPEvent(
                veilederIdent = contextEvent.veilederIdent,
                eventType = RedisEventType.from(contextEvent.eventType),
                verdi = contextEvent.verdi,
                created = contextEvent.created ?: LocalDateTime.now(),
            )
    }
}
