package no.nav.modiacontextholder.rest.model

import kotlinx.serialization.Serializable
import no.nav.modiacontextholder.domain.VeilederContextType

@Serializable
data class RSNyContext(
    val verdi: String = "",
    val eventType: VeilederContextType,
    val verdiType: VerdiType? = VerdiType.FNR,
)

@Serializable
enum class VerdiType {
    FNR,
    FNR_KODE,
}
