package no.nav.modiacontextholder.service

import io.vavr.control.Try
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.types.identer.NavIdent
import no.nav.modiacontextholder.rest.model.DecoratorDomain
import no.nav.modiacontextholder.utils.CacheFactory
import org.slf4j.LoggerFactory

open class EnheterService(
    private val client: AxsysClient,
    private val enheterCache: EnheterCache,
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val cache = CacheFactory.createCache<String, List<DecoratorDomain.Enhet>?>()

    open fun hentEnheter(ident: String): Try<List<DecoratorDomain.Enhet>> {
        val aktiveEnheter = enheterCache.get()

        return Try
            .of {
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
