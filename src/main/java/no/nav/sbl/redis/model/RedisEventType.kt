package no.nav.sbl.redis.model

import no.nav.sbl.domain.ContextEventType

enum class RedisEventType {
    AKTIV_BRUKER,
    AKTIV_ENHET,
    ;

    fun toDomainEventType(): ContextEventType =
        when (this) {
            AKTIV_BRUKER -> ContextEventType.NY_AKTIV_BRUKER
            AKTIV_ENHET -> ContextEventType.NY_AKTIV_ENHET
        }

    companion object {
        fun from(eventType: ContextEventType): RedisEventType =
            when (eventType) {
                ContextEventType.NY_AKTIV_BRUKER -> AKTIV_BRUKER
                ContextEventType.NY_AKTIV_ENHET -> AKTIV_ENHET
            }
    }
}
