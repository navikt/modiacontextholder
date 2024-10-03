package no.nav.modiacontextholder.domain

import kotlinx.datetime.LocalDateTime

data class VeilederContext(
    val veilederIdent: String,
    val contextType: VeilederContextType,
    val created: LocalDateTime? = null,
    val verdi: String,
)
