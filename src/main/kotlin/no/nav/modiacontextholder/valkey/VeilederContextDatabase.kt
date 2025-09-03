package no.nav.modiacontextholder.valkey

import no.nav.modiacontextholder.domain.VeilederContext
import no.nav.modiacontextholder.domain.VeilederContextType

interface VeilederContextDatabase {
    fun save(veilederContext: VeilederContext)

    fun sistAktiveBrukerEvent(veilederIdent: String): VeilederContext?

    fun sistAktiveEnhetEvent(veilederIdent: String): VeilederContext?

    fun sistAktiveGruppeIdEvent(veilederIdent: String): VeilederContext?

    fun slettAlleEventer(veilederIdent: String)

    fun slettAlleAvEventTypeForVeileder(
        contextType: VeilederContextType,
        veilederIdent: String,
    )
}
