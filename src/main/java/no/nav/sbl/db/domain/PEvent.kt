package no.nav.sbl.db.domain

import java.time.LocalDateTime

data class PEvent(
    var id: Long? = null,
    var veilederIdent: String? = null,
    var eventType: String? = null,
    var created: LocalDateTime? = null,
    var verdi: String? = null,
)
