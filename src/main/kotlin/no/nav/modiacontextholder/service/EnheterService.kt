package no.nav.modiacontextholder.service

import kotlinx.serialization.serializer
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.types.identer.NavIdent
import no.nav.modiacontextholder.rest.model.DecoratorDomain
import no.nav.modiacontextholder.service.unleash.ToggleableFeatures
import no.nav.modiacontextholder.service.unleash.UnleashService
import no.nav.modiacontextholder.utils.CacheFactory
import no.nav.modiacontextholder.utils.DistributedCache
import org.slf4j.LoggerFactory

open class EnheterService(
    private val client: AxsysClient,
    private val azureADService: AzureADService,
    private val enheterCache: EnheterCache,
    private val unleashService: UnleashService,
    private val cache: DistributedCache<String, List<DecoratorDomain.Enhet>?> =
        CacheFactory.createDistributedCache(
            name = "enheter",
            serializer = serializer<List<DecoratorDomain.Enhet>?>(),
        ),
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    open suspend fun hentEnheter(
        ident: String,
        token: String? = null,
    ): Result<List<DecoratorDomain.Enhet>> {
        if (unleashService.isEnabled(ToggleableFeatures.ENHETER_I_AZURE) && token !== null) {
            return hentEnheterFraAzure(ident, token)
        }
        val aktiveEnheter = enheterCache.get()

        return runCatching {
            cache
                .get(ident) {
                    client
                        .hentTilganger(NavIdent.of(ident))
                        .mapNotNull { enhet -> aktiveEnheter[enhet.enhetId.get()] }
                        .sortedBy { it.enhetId }
                        .ifEmpty { null }
                }.orEmpty()
        }.onFailure { exception -> log.error("Kunne ikke hente enheter for $ident fra AXSYS", exception) }
    }

    private suspend fun hentEnheterFraAzure(
        ident: String,
        token: String,
    ): Result<List<DecoratorDomain.Enhet>> {
        val aktiveEnheter = enheterCache.get()

        return runCatching {
            azureADService
                .fetchRoller(token, NavIdent.of(ident))
                .mapNotNull {
                    enhetId(it.gruppeId)?.let { enhetId ->
                        aktiveEnheter[enhetId]
                    }
                }.sortedBy { it.enhetId }
        }.onFailure { exception ->
            log.error("Kunne ikke hente enheter for $ident fra Entra ID", exception)
        }
    }

    private fun enhetId(gruppeId: String): String? = Regex("0000-GA-ENHET_(.*)").find(gruppeId)?.groupValues?.get(1)

    open fun hentAlleEnheter(): List<DecoratorDomain.Enhet> = enheterCache.getAll()
}
