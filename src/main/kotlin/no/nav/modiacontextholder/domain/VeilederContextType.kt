package no.nav.modiacontextholder.domain

import kotlinx.serialization.Serializable

@Serializable
enum class VeilederContextType {
    NY_AKTIV_BRUKER,
    NY_AKTIV_ENHET,
    MY_AKTIV_GRUPPE_ID,
}
