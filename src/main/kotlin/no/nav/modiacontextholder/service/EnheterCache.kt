package no.nav.modiacontextholder.service

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.nav.modiacontextholder.consumers.norg2.Norg2Client
import no.nav.modiacontextholder.consumers.norg2.domain.Enhet
import no.nav.modiacontextholder.rest.model.DecoratorDomain
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class EnheterCache(
    private val norg2Client: Norg2Client,
) {
    companion object {
        private const val HVER_TOLVTE_TIME: Long = 12 * 3600 * 1000
    }

    private val log = LoggerFactory.getLogger(EnheterCache::class.java)
    private var cache: Map<String, DecoratorDomain.Enhet> = Collections.unmodifiableMap(HashMap())
    private var cacheList: List<DecoratorDomain.Enhet> = Collections.unmodifiableList(ArrayList())

    private val executor = Executors.newSingleThreadScheduledExecutor()

    init {
        schedule()
    }

    private fun schedule() {
        val task =
            Runnable {
                refreshCache()
            }

        GlobalScope.launch {
            executor.scheduleWithFixedDelay(task, 0, HVER_TOLVTE_TIME, TimeUnit.MILLISECONDS)
        }
    }

    private fun refreshCache() {
        log.info("Henter enheter fra NORG2 for populering av cache")
        try {
            val enheter: List<Enhet> = norg2Client.hentAlleEnheter()

            cacheList =
                Collections.unmodifiableList(
                    enheter
                        .map { enhet -> DecoratorDomain.Enhet(enhet.enhetNr, enhet.navn) }
                        .sortedBy { it.enhetId },
                )

            cache =
                Collections.unmodifiableMap(
                    cacheList
                        .associateBy { it.enhetId },
                )
        } catch (e: Exception) {
            log.error("Kunne ikke hente ut alle aktive enheter fra NORG2-rest", e)
        }
    }

    fun get(): Map<String, DecoratorDomain.Enhet> = this.cache

    fun getAll(): List<DecoratorDomain.Enhet> = cacheList
}
