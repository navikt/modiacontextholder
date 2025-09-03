package no.nav.modiacontextholder.service

import kotlinx.coroutines.runBlocking
import no.nav.common.json.JsonUtils
import no.nav.modiacontextholder.domain.VeilederContext
import no.nav.modiacontextholder.domain.VeilederContextType
import no.nav.modiacontextholder.valkey.ValkeyPublisher
import no.nav.modiacontextholder.valkey.VeilederContextDatabase
import no.nav.modiacontextholder.rest.model.RSAktivBruker
import no.nav.modiacontextholder.rest.model.RSAktivEnhet
import no.nav.modiacontextholder.rest.model.RSContext
import no.nav.modiacontextholder.rest.model.RSEvent
import no.nav.modiacontextholder.rest.model.RSNyContext
import org.slf4j.LoggerFactory

class ContextService(
    private val veilederContextDatabase: VeilederContextDatabase,
    private val valkeyPublisher: ValkeyPublisher,
    private val enheterService: EnheterService
) {
    private val log = LoggerFactory.getLogger(ContextService::class.java)

    fun hentVeiledersContext(veilederIdent: String): RSContext =
        RSContext(
            hentAktivBruker(veilederIdent).aktivBruker,
            hentAktivEnhet(veilederIdent).aktivEnhet,
            hentAktivGruppeId(veilederIdent).aktivGruppeId,
        )

    fun oppdaterVeiledersContext(
        nyContext: RSNyContext,
        veilederIdent: String,
    ) {
        val veilederContext =
            VeilederContext(
                verdi = nyContext.verdi,
                contextType = nyContext.eventType,
                veilederIdent = veilederIdent,
            )

        if (nyContext.eventType == VeilederContextType.NY_AKTIV_BRUKER && nyContext.verdi.isEmpty()) {
            nullstillAktivBruker(veilederIdent)
            return
        } else if (nyContext.verdi.isEmpty()) {
            log.warn("Forsøk på å sette aktivEnhet til null, vil generere feil.")
        }

        saveToDb(veilederContext)

        valkeyPublisher.publishMessage(JsonUtils.toJson(RSEvent.from(veilederContext)))
    }

    suspend fun oppdaterVeiledersGroupId(
        nyContext: RSNyContext,
        veilederIdent: String,
        token: String
    ) {
        val gruppeId = enheterService.hentEnheter(veilederIdent, token).getOrNull()
            ?.find { it.enhetId == nyContext.verdi }?.gruppeId
        if (gruppeId != null) {
            saveToDb(
                VeilederContext(
                    verdi = gruppeId,
                    contextType = VeilederContextType.MY_AKTIV_GRUPPE_ID,
                    veilederIdent = veilederIdent,
                )
            )
        }
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

    fun hentAktivGruppeId(veilederIdent: String): RSContext =
        veilederContextDatabase
            .sistAktiveGruppeIdEvent(veilederIdent)
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
