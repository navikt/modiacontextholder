package no.nav.sbl.service

import no.nav.sbl.db.dao.EventDAO
import no.nav.sbl.rest.domain.RSEvent

class EventService(
    private val eventDAO: EventDAO
) {
    fun hentEventerEtterId(id: Long): List<RSEvent> {
        return eventDAO.finnAlleEventerEtterId(id)
            .map(RSEvent::from)
    }
}