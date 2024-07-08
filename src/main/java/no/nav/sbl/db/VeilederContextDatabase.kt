package no.nav.sbl.db

import no.nav.sbl.db.domain.PEvent

interface VeilederContextDatabase {
    fun save(pEvent: PEvent)

    fun sistAktiveBrukerEvent(veilederIdent: String): PEvent?

    fun sistAktiveEnhetEvent(veilederIdent: String): PEvent?

    fun slettAlleEventer(veilederIdent: String)

    fun slettAlleAvEventTypeForVeileder(
        eventType: String,
        veilederIdent: String,
    )
}
