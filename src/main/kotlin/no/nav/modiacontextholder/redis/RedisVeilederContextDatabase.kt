package no.nav.modiacontextholder.redis

import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.common.health.HealthCheckResult
import no.nav.common.health.selftest.SelfTestCheck
import no.nav.modiacontextholder.domain.VeilederContext
import no.nav.modiacontextholder.domain.VeilederContextType
import no.nav.modiacontextholder.infrastructur.HealthCheckAware
import no.nav.modiacontextholder.redis.model.RedisPEvent
import no.nav.modiacontextholder.redis.model.RedisPEventKey
import no.nav.modiacontextholder.redis.model.RedisVeilederContextType
import no.nav.personoversikt.common.utils.SelftestGenerator
import java.time.Duration
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RedisVeilederContextDatabase(
    redisConnection: StatefulRedisConnection<String, String>,
) : VeilederContextDatabase,
    HealthCheckAware {
    private val timeToLive = Duration.ofHours(12L)

    private val redis = redisConnection.sync()
    private val reporter = SelftestGenerator.Reporter(name = "Veileder database", critical = true)

    init {
        fixedRateTimer(
            name = "Veileder database health",
            daemon = false,
            period = 1.minutes.inWholeMilliseconds,
            initialDelay = 1.seconds.inWholeMilliseconds,
        ) {
            runCatching { redis.ping() }
                .onFailure { reporter.reportError(it) }
                .onSuccess { reporter.reportOk() }
        }
    }

    override fun save(veilederContext: VeilederContext) {
        val redisPEvent = RedisPEvent.from(veilederContext)
        val json = Json.encodeToString(redisPEvent)

        val redisKey = redisPEvent.key.toString()
        when (redisPEvent.contextType) {
            RedisVeilederContextType.AKTIV_BRUKER -> redis.setex(redisKey, timeToLive.seconds, json)
            RedisVeilederContextType.AKTIV_ENHET -> redis.set(redisKey, json)
        }
    }

    override fun sistAktiveBrukerEvent(veilederIdent: String): VeilederContext? {
        val key = RedisPEventKey(RedisVeilederContextType.AKTIV_BRUKER, veilederIdent)

        val result = redis.get(key.toString()) ?: return null
        return Json.decodeFromString<RedisPEvent>(result).toPEvent()
    }

    override fun sistAktiveEnhetEvent(veilederIdent: String): VeilederContext? {
        val key = RedisPEventKey(RedisVeilederContextType.AKTIV_ENHET, veilederIdent)

        val result = redis.get(key.toString()) ?: return null
        return Json.decodeFromString<RedisPEvent>(result).toPEvent()
    }

    override fun slettAlleEventer(veilederIdent: String) {
        val keys =
            RedisVeilederContextType.entries
                .map { RedisPEventKey(it, veilederIdent).toString() }
                .toTypedArray()
        redis.del(*keys)
    }

    override fun slettAlleAvEventTypeForVeileder(
        contextType: VeilederContextType,
        veilederIdent: String,
    ) {
        val key = RedisPEventKey(RedisVeilederContextType.from(contextType), veilederIdent)
        redis.del(key.toString())
    }

    private fun checkHealth(): HealthCheckResult =
        try {
            redis.ping()
            HealthCheckResult.healthy()
        } catch (e: Exception) {
            HealthCheckResult.unhealthy(e)
        }

    override fun getHealthCheck(): SelfTestCheck = SelfTestCheck("Veileder context database", true) { checkHealth() }
}
