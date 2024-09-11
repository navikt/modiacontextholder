package no.nav.modiacontextholder.service

import no.nav.common.json.JsonUtils
import no.nav.modiacontextholder.domain.VeilederContext
import no.nav.modiacontextholder.domain.VeilederContextType
import no.nav.modiacontextholder.redis.RedisPublisher
import no.nav.modiacontextholder.redis.VeilederContextDatabase
import no.nav.modiacontextholder.rest.model.RSAktivBruker
import no.nav.modiacontextholder.rest.model.RSAktivEnhet
import no.nav.modiacontextholder.rest.model.RSContext
import no.nav.modiacontextholder.rest.model.RSEvent
import no.nav.modiacontextholder.rest.model.RSNyContext
import org.slf4j.LoggerFactory

class ContextService(
    private val veilederContextDatabase: VeilederContextDatabase,
    private val redisPublisher: RedisPublisher,
) {
    private val log = LoggerFactory.getLogger(ContextService::class.java)

    fun hentVeiledersContext(veilederIdent: String): RSContext =
        RSContext(
            hentAktivBruker(veilederIdent).aktivBruker,
            hentAktivEnhet(veilederIdent).aktivEnhet,
        )

    fun oppdaterVeiledersContext(
        nyContext: RSNyContext,
        veilederIdent: String,
    ) {
        val veilederContext =
            VeilederContext(
                verdi = nyContext.verdi,
                contextType = VeilederContextType.valueOf(nyContext.eventType),
                veilederIdent = veilederIdent,
            )

        if (VeilederContextType.NY_AKTIV_BRUKER.name == nyContext.eventType && nyContext.verdi.isEmpty()) {
            nullstillAktivBruker(veilederIdent)
            return
        } else if (nyContext.verdi.isEmpty()) {
            log.warn("Forsøk på å sette aktivEnhet til null, vil generere feil.")
        }

        saveToDb(veilederContext)

        redisPublisher.publishMessage(JsonUtils.toJson(RSEvent.from(veilederContext)))
    }

    fun hentAktivBruker(veilederIdent: String): RSContext =
        veilederContextDatabase
            .sistAktiveBrukerEvent(veilederIdent)
            ?.let(RSContext::from)
            ?: RSContext()

    fun hentAktivBrukerV2(veilederIdent: String): RSAktivBruker =
        veilederContextDatabase
            .sistAktiveBrukerEvent(veilederIdent)
            ?.let(RSAktivBruker::from)
            ?: RSAktivBruker(null)

    fun hentAktivEnhet(veilederIdent: String): RSContext =
        veilederContextDatabase
            .sistAktiveEnhetEvent(veilederIdent)
            ?.let(RSContext::from)
            ?: RSContext()

    fun hentAktivEnhetV2(veilederIdent: String): RSAktivEnhet =
        veilederContextDatabase
            .sistAktiveEnhetEvent(veilederIdent)
            ?.let(RSAktivEnhet::from)
            ?: RSAktivEnhet(null)

    fun nullstillContext(veilederIdent: String) {
        veilederContextDatabase.slettAlleEventer(veilederIdent)
    }

    fun nullstillAktivBruker(veilederIdent: String) {
        veilederContextDatabase.slettAlleAvEventTypeForVeileder(
            VeilederContextType.NY_AKTIV_BRUKER,
            veilederIdent,
        )
    }

    private fun saveToDb(event: VeilederContext) = veilederContextDatabase.save(event)
}
