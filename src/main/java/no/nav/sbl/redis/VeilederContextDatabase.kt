package no.nav.sbl.redis

import no.nav.sbl.domain.ContextEvent

interface VeilederContextDatabase {
    fun save(contextEvent: ContextEvent)

    fun sistAktiveBrukerEvent(veilederIdent: String): ContextEvent?

    fun sistAktiveEnhetEvent(veilederIdent: String): ContextEvent?

    fun slettAlleEventer(veilederIdent: String)

    fun slettAlleAvEventTypeForVeileder(
        eventType: String,
        veilederIdent: String,
    )
}
