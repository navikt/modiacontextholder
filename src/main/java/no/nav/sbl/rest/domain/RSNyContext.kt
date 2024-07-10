package no.nav.sbl.rest.domain;

data class RSNyContext(
    val verdi: String,
    val eventType: String,
    val verdiType: VerdiType? = VerdiType.FNR,
)

enum class VerdiType {
    FNR,
    FNR_KODE
}