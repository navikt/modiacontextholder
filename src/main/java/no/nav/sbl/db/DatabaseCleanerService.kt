package no.nav.sbl.db

import io.micrometer.core.annotation.Timed
import no.nav.sbl.db.dao.EventDAO
import no.nav.sbl.db.domain.EventType
import org.springframework.scheduling.annotation.Scheduled
import java.util.function.Consumer

class DatabaseCleanerService(
    private val eventDAO: EventDAO,
) {
    @Scheduled(cron = "0 0 2 * * *")
    @Timed("slettAlleNyAktivBrukerEvents")
    fun slettAlleNyAktivBrukerEvents() {
        eventDAO.slettAlleAvEventType(EventType.NY_AKTIV_BRUKER.name)
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Timed("slettAlleUtenomSisteNyAktivEnhet")
    fun slettAlleUtenomSisteNyAktivEnhet() {
        eventDAO
            .hentUnikeVeilederIdenter()
            .forEach(
                Consumer { veilederIdent: String? ->
                    val eventer = eventDAO.hentVeiledersEventerAvType(EventType.NY_AKTIV_ENHET.name, veilederIdent!!)
                    eventDAO.slettAlleEventerUtenomNyeste(eventer)
                },
            )
    }
}
