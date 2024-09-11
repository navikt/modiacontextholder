package no.nav.modiacontextholder.redis.model

import no.nav.modiacontextholder.domain.VeilederContextType

enum class RedisVeilederContextType {
    AKTIV_BRUKER,
    AKTIV_ENHET,
    ;

    fun toDomain(): VeilederContextType =
        when (this) {
            AKTIV_BRUKER -> VeilederContextType.NY_AKTIV_BRUKER
            AKTIV_ENHET -> VeilederContextType.NY_AKTIV_ENHET
        }

    companion object {
        fun from(contextType: VeilederContextType): RedisVeilederContextType =
            when (contextType) {
                VeilederContextType.NY_AKTIV_BRUKER -> AKTIV_BRUKER
                VeilederContextType.NY_AKTIV_ENHET -> AKTIV_ENHET
            }
    }
}
