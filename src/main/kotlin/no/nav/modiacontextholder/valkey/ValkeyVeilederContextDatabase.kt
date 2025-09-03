package no.nav.modiacontextholder.valkey

import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.serialization.json.Json
import no.nav.common.health.HealthCheckResult
import no.nav.common.health.selftest.SelfTestCheck
import no.nav.modiacontextholder.domain.VeilederContext
import no.nav.modiacontextholder.domain.VeilederContextType
import no.nav.modiacontextholder.infrastructur.HealthCheckAware
import no.nav.modiacontextholder.valkey.model.ValkeyPEvent
import no.nav.modiacontextholder.valkey.model.ValkeyPEventKey
import no.nav.modiacontextholder.valkey.model.ValkeyVeilederContextType
import no.nav.personoversikt.common.utils.SelftestGenerator
import java.time.Duration
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ValkeyVeilederContextDatabase(
    redisConnection: StatefulRedisConnection<String, String>,
) : VeilederContextDatabase,
    HealthCheckAware {
    private val timeToLive = Duration.ofHours(3L)

    private val valkey = redisConnection.sync()
    private val reporter = SelftestGenerator.Reporter(name = "Veileder database", critical = true)

    init {
        fixedRateTimer(
            name = "Veileder database health",
            daemon = false,
            period = 1.minutes.inWholeMilliseconds,
            initialDelay = 1.seconds.inWholeMilliseconds,
        ) {
            runCatching { valkey.ping() }
                .onFailure { reporter.reportError(it) }
                .onSuccess { reporter.reportOk() }
        }
    }

    override fun save(veilederContext: VeilederContext) {
        val valkeyPEvent = ValkeyPEvent.from(veilederContext)
        val json = Json.encodeToString(valkeyPEvent)

        val key = valkeyPEvent.key.toString()
        when (valkeyPEvent.contextType) {
            ValkeyVeilederContextType.AKTIV_BRUKER -> valkey.setex(key, timeToLive.seconds, json)
            ValkeyVeilederContextType.AKTIV_ENHET -> valkey.set(key, json)
            ValkeyVeilederContextType.AKTIV_GRUPPE_ID -> valkey.set(key, json)
        }
    }

    override fun sistAktiveBrukerEvent(veilederIdent: String): VeilederContext? {
        val key = ValkeyPEventKey(ValkeyVeilederContextType.AKTIV_BRUKER, veilederIdent)

        val result = valkey.get(key.toString()) ?: return null
        return Json.decodeFromString<ValkeyPEvent>(result).toPEvent()
    }

    override fun sistAktiveEnhetEvent(veilederIdent: String): VeilederContext? {
        val key = ValkeyPEventKey(ValkeyVeilederContextType.AKTIV_ENHET, veilederIdent)

        val result = valkey.get(key.toString()) ?: return null
        return Json.decodeFromString<ValkeyPEvent>(result).toPEvent()
    }

    override fun sistAktiveGruppeIdEvent(veilederIdent: String): VeilederContext? {
        val key = ValkeyPEventKey(ValkeyVeilederContextType.AKTIV_GRUPPE_ID, veilederIdent)
        val result = valkey.get(key.toString()) ?: return null
        return Json.decodeFromString<ValkeyPEvent>(result).toPEvent()
    }

    override fun slettAlleEventer(veilederIdent: String) {
        val keys =
            ValkeyVeilederContextType.entries
                .map { ValkeyPEventKey(it, veilederIdent).toString() }
                .toTypedArray()
        valkey.del(*keys)
    }

    override fun slettAlleAvEventTypeForVeileder(
        contextType: VeilederContextType,
        veilederIdent: String,
    ) {
        val key = ValkeyPEventKey(ValkeyVeilederContextType.from(contextType), veilederIdent)
        valkey.del(key.toString())
    }

    private fun checkHealth(): HealthCheckResult =
        try {
            valkey.ping()
            HealthCheckResult.healthy()
        } catch (e: Exception) {
            HealthCheckResult.unhealthy(e)
        }

    override fun getHealthCheck(): SelfTestCheck = SelfTestCheck("Veileder context database", true) { checkHealth() }
}
