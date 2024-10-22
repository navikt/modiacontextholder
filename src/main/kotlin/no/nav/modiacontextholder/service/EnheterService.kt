package no.nav.modiacontextholder.service

import kotlinx.serialization.serializer
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.types.identer.NavIdent
import no.nav.modiacontextholder.rest.model.DecoratorDomain
import no.nav.modiacontextholder.utils.CacheFactory
import no.nav.modiacontextholder.utils.DistributedCache
import org.slf4j.LoggerFactory

open class EnheterService(
    private val client: AxsysClient,
    private val enheterCache: EnheterCache,
    private val cache: DistributedCache<String, List<DecoratorDomain.Enhet>?> =
        CacheFactory.createDistributedCache(
            name = "enheter",
            serializer = serializer<List<DecoratorDomain.Enhet>?>(),
        ),
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    open suspend fun hentEnheter(ident: String): Result<List<DecoratorDomain.Enhet>> {
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

    open fun hentAlleEnheter(): List<DecoratorDomain.Enhet> = enheterCache.getAll()
}
