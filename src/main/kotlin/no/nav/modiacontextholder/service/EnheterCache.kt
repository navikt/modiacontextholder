package no.nav.modiacontextholder.service

import no.nav.common.health.HealthCheckResult
import no.nav.common.health.selftest.SelfTestCheck
import no.nav.modiacontextholder.consumers.norg2.Norg2Client
import no.nav.modiacontextholder.consumers.norg2.domain.Enhet
import no.nav.modiacontextholder.infrastructur.HealthCheckAware
import no.nav.modiacontextholder.rest.model.DecoratorDomain
import no.nav.personoversikt.common.utils.SelftestGenerator
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class EnheterCache(
    private val norg2Client: Norg2Client,
) : HealthCheckAware {
    private val log = LoggerFactory.getLogger(EnheterCache::class.java)
    private var cache: Map<String, DecoratorDomain.Enhet> = Collections.unmodifiableMap(HashMap())
    private var cacheList: List<DecoratorDomain.Enhet> = Collections.unmodifiableList(ArrayList())

    private val reporter = SelftestGenerator.Reporter(name = "Enheter cache", critical = true)

    init {
        fixedRateTimer(
            name = "Refresh enheter cache",
            daemon = true,
            period = 6.hours.inWholeMilliseconds,
            initialDelay = 0,
        ) {
            refreshCache()
        }

        fixedRateTimer(
            name = "Enheter cache health",
            daemon = false,
            period = 1.minutes.inWholeMilliseconds,
            initialDelay = 1.seconds.inWholeMilliseconds,
        ) {
            getHealthCheckResult()
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

            log.info("Cache lastet inn med ${cache.count()} enheter")
        } catch (e: Exception) {
            log.error("Kunne ikke hente ut alle aktive enheter fra NORG2-rest", e)
        }
    }

    fun get(): Map<String, DecoratorDomain.Enhet> = this.cache

    fun getAll(): List<DecoratorDomain.Enhet> = cacheList

    private fun getHealthCheckResult(): HealthCheckResult =
        if (cache.isNotEmpty()) {
            reporter.reportOk()
            HealthCheckResult.healthy()
        } else {
            reporter.reportError(Error("EnheterCache is empty!"))
            HealthCheckResult.unhealthy("EnheterCache is empty")
        }

    override fun getHealthCheck(): SelfTestCheck = SelfTestCheck("Enheter cache", true) { getHealthCheckResult() }
}
