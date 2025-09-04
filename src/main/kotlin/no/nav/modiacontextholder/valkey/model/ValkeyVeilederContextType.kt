package no.nav.modiacontextholder.valkey.model

import no.nav.modiacontextholder.domain.VeilederContextType

enum class ValkeyVeilederContextType {
    AKTIV_BRUKER,
    AKTIV_ENHET,
    AKTIV_GRUPPE_ID
    ;

    fun toDomain(): VeilederContextType =
        when (this) {
            AKTIV_BRUKER -> VeilederContextType.NY_AKTIV_BRUKER
            AKTIV_ENHET -> VeilederContextType.NY_AKTIV_ENHET
            AKTIV_GRUPPE_ID -> VeilederContextType.NY_AKTIV_GRUPPE_ID
        }

    companion object {
        fun from(contextType: VeilederContextType): ValkeyVeilederContextType =
            when (contextType) {
                VeilederContextType.NY_AKTIV_BRUKER -> AKTIV_BRUKER
                VeilederContextType.NY_AKTIV_ENHET -> AKTIV_ENHET
                VeilederContextType.NY_AKTIV_GRUPPE_ID -> AKTIV_GRUPPE_ID
            }
    }
}
