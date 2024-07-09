package no.nav.sbl.domain

import java.time.LocalDateTime

data class VeilederContext(
    val veilederIdent: String,
    val contextType: VeilederContextType,
    val created: LocalDateTime? = null,
    val verdi: String,
)
