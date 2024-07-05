package no.nav.sbl.db

import no.nav.sbl.db.domain.PEvent

interface VeilederContextDatabase {
    fun save(pEvent: PEvent): Long

    fun sistAktiveBrukerEvent(veilederIdent: String): PEvent?

    fun sistAktiveEnhetEvent(veilederIdent: String): PEvent?

    fun finnAlleEventerEtterId(id: Long): List<PEvent>

    fun slettAllEventer(veilederIdent: String)

    fun slettAlleAvEventTypeForVeileder(
        eventType: String,
        veilederIdent: String,
    )
}
