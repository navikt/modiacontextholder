package no.nav.modiacontextholder.service

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.serializer
import no.nav.common.types.identer.NavIdent
import no.nav.modiacontextholder.rest.model.DecoratorDomain
import no.nav.modiacontextholder.utils.CacheFactory
import no.nav.modiacontextholder.utils.DistributedCache
import org.slf4j.LoggerFactory
import kotlin.text.get

open class EnheterService(
    private val azureADService: AzureADService,
    private val enheterCache: EnheterCache,
    private val cache: DistributedCache<String, List<DecoratorDomain.Enhet>?> =
        CacheFactory.createDistributedCache(
            name = "enheter",
            serializer = serializer<List<DecoratorDomain.Enhet>?>(),
        ),
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun hentEnheter(
        ident: String,
        token: String,
    ): Result<List<DecoratorDomain.Enhet>> {
        val aktiveEnheter = enheterCache.get()

        return runCatching {
            cache.get(ident) {
                runBlocking {
                    azureADService
                        .fetchRoller(token, NavIdent.of(ident))
                        .mapNotNull {
                            enhetId(it.gruppeNavn)?.let { enhetId ->
                                aktiveEnheter[enhetId]
                            }?.copy(gruppeId = it.gruppeId)
                        }.sortedBy { it.enhetId }
                        .ifEmpty { null }
                }
            }.orEmpty()
        }.onFailure { exception ->
            log.error("Kunne ikke hente enheter for $ident fra Entra ID", exception)
        }
    }

    private fun enhetId(gruppeNavn: String): String? = Regex("0000-GA-ENHET_(.*)").find(gruppeNavn)?.groupValues?.get(1)

    open fun hentAlleEnheter(): List<DecoratorDomain.Enhet> = enheterCache.getAll()
}
