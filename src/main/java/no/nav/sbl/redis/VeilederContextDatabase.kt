package no.nav.sbl.redis

import no.nav.sbl.domain.VeilederContext
import no.nav.sbl.domain.VeilederContextType

interface VeilederContextDatabase {
    fun save(veilederContext: VeilederContext)

    fun sistAktiveBrukerEvent(veilederIdent: String): VeilederContext?

    fun sistAktiveEnhetEvent(veilederIdent: String): VeilederContext?

    fun slettAlleEventer(veilederIdent: String)

    fun slettAlleAvEventTypeForVeileder(
        eventType: VeilederContextType,
        veilederIdent: String,
    )
}
