package no.nav.sbl.redis.model

import no.nav.sbl.db.domain.PEvent

data class RedisPEvent(
    val veilederIdent: String,
    val eventType: RedisEventType,
    val verdi: String,
) {
    val key: RedisPEventKey = RedisPEventKey(eventType, veilederIdent)

    fun toPEvent(): PEvent =
        PEvent(
            veilederIdent = veilederIdent,
            eventType = eventType.toDomainEventType(),
            verdi = verdi,
        )

    companion object {
        fun from(pEvent: PEvent): RedisPEvent =
            RedisPEvent(
                veilederIdent = pEvent.veilederIdent!!,
                eventType = RedisEventType.from(pEvent.eventType!!),
                verdi = pEvent.verdi!!,
            )
    }
}
