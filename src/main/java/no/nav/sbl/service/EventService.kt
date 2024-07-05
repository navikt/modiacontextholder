package no.nav.sbl.service

import no.nav.sbl.db.VeilederContextDatabase
import no.nav.sbl.rest.domain.RSEvent

class EventService(
    private val veilederContextDatabase: VeilederContextDatabase,
) {
    fun hentEventerEtterId(id: Long): List<RSEvent> =
        veilederContextDatabase
            .finnAlleEventerEtterId(id)
            .map(RSEvent::from)
}
