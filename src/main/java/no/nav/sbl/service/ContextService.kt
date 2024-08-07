package no.nav.sbl.service

import no.nav.common.json.JsonUtils
import no.nav.sbl.config.ApplicationCluster
import no.nav.sbl.consumers.modiacontextholder.ModiaContextHolderClient
import no.nav.sbl.domain.VeilederContext
import no.nav.sbl.domain.VeilederContextType
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.redis.VeilederContextDatabase
import no.nav.sbl.rest.model.RSAktivBruker
import no.nav.sbl.rest.model.RSAktivEnhet
import no.nav.sbl.rest.model.RSContext
import no.nav.sbl.rest.model.RSEvent
import no.nav.sbl.rest.model.RSNyContext
import no.nav.sbl.service.unleash.ToggleableFeatureService
import no.nav.sbl.service.unleash.ToggleableFeatures
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ContextService(
    private val veilederContextDatabase: VeilederContextDatabase,
    private val redisPublisher: RedisPublisher,
    private val contextHolderClient: ModiaContextHolderClient,
    private val toggleableFeatureService: ToggleableFeatureService,
) {
    private val log = LoggerFactory.getLogger(ContextService::class.java)

    fun hentVeiledersContext(veilederIdent: String): RSContext =
        if (burdeSynceContextMedGcp()) {
            contextHolderClient
                .hentVeiledersContext(veilederIdent)
                .getOrThrow()
        } else {
            RSContext(
                hentAktivBruker(veilederIdent).aktivBruker,
                hentAktivEnhet(veilederIdent).aktivEnhet,
            )
        }

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
        if (burdeSynceContextMedGcp()) {
            contextHolderClient
                .oppdaterVeiledersContext(nyContext, veilederIdent)
                .getOrThrow()
        } else {
            if (VeilederContextType.NY_AKTIV_BRUKER.name == nyContext.eventType && nyContext.verdi.isEmpty()) {
                nullstillAktivBruker(veilederIdent)
                return
            } else if (nyContext.verdi.isEmpty()) {
                log.warn("Forsøk på å sette aktivEnhet til null, vil generere feil.")
            }

            saveToDb(veilederContext)
        }

        redisPublisher.publishMessage(JsonUtils.toJson(RSEvent.from(veilederContext)))
    }

    fun hentAktivBruker(veilederIdent: String): RSContext =
        if (burdeSynceContextMedGcp()) {
            contextHolderClient
                .hentAktivBruker(veilederIdent)
                .getOrThrow()
        } else {
            veilederContextDatabase
                .sistAktiveBrukerEvent(veilederIdent)
                ?.let(RSContext::from)
                ?: RSContext()
        }

    fun hentAktivBrukerV2(veilederIdent: String): RSAktivBruker =
        if (burdeSynceContextMedGcp()) {
            contextHolderClient
                .hentAktivBrukerV2(veilederIdent)
                .getOrThrow()
        } else {
            veilederContextDatabase
                .sistAktiveBrukerEvent(veilederIdent)
                ?.let(RSAktivBruker::from)
                ?: RSAktivBruker(null)
        }

    fun hentAktivEnhet(veilederIdent: String): RSContext =
        if (burdeSynceContextMedGcp()) {
            contextHolderClient
                .hentAktivEnhet(veilederIdent)
                .getOrThrow()
        } else {
            veilederContextDatabase
                .sistAktiveEnhetEvent(veilederIdent)
                ?.let(RSContext::from)
                ?: RSContext()
        }

    fun hentAktivEnhetV2(veilederIdent: String): RSAktivEnhet =
        if (burdeSynceContextMedGcp()) {
            contextHolderClient
                .hentAktivEnhetV2(veilederIdent)
                .getOrThrow()
        } else {
            veilederContextDatabase
                .sistAktiveEnhetEvent(veilederIdent)
                ?.let(RSAktivEnhet::from)
                ?: RSAktivEnhet(null)
        }

    fun nullstillContext(veilederIdent: String) {
        if (burdeSynceContextMedGcp()) {
            contextHolderClient.nullstillBrukerContext(veilederIdent)
        } else {
            veilederContextDatabase.slettAlleEventer(veilederIdent)
        }
    }

    fun nullstillAktivBruker(veilederIdent: String) {
        if (burdeSynceContextMedGcp()) {
            contextHolderClient.nullstillAktivBruker(veilederIdent)
        } else {
            veilederContextDatabase.slettAlleAvEventTypeForVeileder(
                VeilederContextType.NY_AKTIV_BRUKER,
                veilederIdent,
            )
        }
    }

    private fun saveToDb(event: VeilederContext) = veilederContextDatabase.save(event)

    private fun burdeSynceContextMedGcp(): Boolean =
        ApplicationCluster.isFss() && toggleableFeatureService.isEnabled(ToggleableFeatures.SYNC_CONTEXT_MED_GCP)
}
