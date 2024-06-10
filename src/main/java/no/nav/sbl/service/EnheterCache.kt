package no.nav.sbl.service

import no.nav.sbl.consumers.norg2.Norg2Client
import no.nav.sbl.consumers.norg2.domain.Enhet
import no.nav.sbl.rest.domain.DecoratorDomain
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import java.util.*

class EnheterCache(
    @Autowired private val norg2Client: Norg2Client
) {
    companion object {
        private const val HVER_TOLVTE_TIME: Long = 12 * 3600 * 1000
    }

    private val log = LoggerFactory.getLogger(EnheterCache::class.java)
    private var cache: Map<String, DecoratorDomain.Enhet> = Collections.unmodifiableMap(HashMap())
    private var cacheList: List<DecoratorDomain.Enhet> = Collections.unmodifiableList(ArrayList())

    @Scheduled(fixedRate = HVER_TOLVTE_TIME)
    private fun refreshCache() {
        try {
            val enheter: List<Enhet> = norg2Client.hentAlleEnheter()

            cacheList = Collections.unmodifiableList(enheter
                .map { enhet -> DecoratorDomain.Enhet(enhet.enhetNr, enhet.navn) }
                .sortedBy { it.enhetId }
            )

            cache = Collections.unmodifiableMap(cacheList
                .associateBy { it.enhetId }
            )

        } catch (e: Exception) {
            log.error("Kunne ikke hente ut alle aktive enheter fra NORG2-rest", e)
        }
    }

    fun get(): Map<String, DecoratorDomain.Enhet> {
        return this.cache
    }

    fun getAll(): List<DecoratorDomain.Enhet> {
        return cacheList
    }
}