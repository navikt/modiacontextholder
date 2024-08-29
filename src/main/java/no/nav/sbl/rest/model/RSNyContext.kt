package no.nav.sbl.rest.model

data class RSNyContext(
    val verdi: String,
    val eventType: String,
    val verdiType: VerdiType? = VerdiType.FNR,
)

enum class VerdiType {
    FNR,
    FNR_KODE
}
