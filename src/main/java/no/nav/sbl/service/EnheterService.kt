package no.nav.sbl.service

import io.vavr.control.Try
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.types.identer.NavIdent
import no.nav.sbl.rest.domain.DecoratorDomain
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable

open class EnheterService(
    private val client: AxsysClient,
    private val enheterCache: EnheterCache,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Cacheable("enheterCache")
    open fun hentEnheter(ident: String): Try<List<DecoratorDomain.Enhet>> {
        val aktiveEnheter = enheterCache.get()
        return Try.of {
            client.hentTilganger(NavIdent.of(ident))
                .mapNotNull { enhet -> aktiveEnheter[enhet.enhetId.get()] }
                .sortedBy { it.enhetId }
        }.onFailure { exception -> log.error("Kunne ikke hente enheter for $ident fra AXSYS", exception) }
    }

    fun hentAlleEnheter(): List<DecoratorDomain.Enhet> {
        return enheterCache.getAll()
    }
}