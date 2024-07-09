package no.nav.sbl.redis.model

import no.nav.sbl.domain.VeilederContextType

enum class RedisEventType {
    AKTIV_BRUKER,
    AKTIV_ENHET,
    ;

    fun toDomainEventType(): VeilederContextType =
        when (this) {
            AKTIV_BRUKER -> VeilederContextType.NY_AKTIV_BRUKER
            AKTIV_ENHET -> VeilederContextType.NY_AKTIV_ENHET
        }

    companion object {
        fun from(eventType: VeilederContextType): RedisEventType =
            when (eventType) {
                VeilederContextType.NY_AKTIV_BRUKER -> AKTIV_BRUKER
                VeilederContextType.NY_AKTIV_ENHET -> AKTIV_ENHET
            }
    }
}
