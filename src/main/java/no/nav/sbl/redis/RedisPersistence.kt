package no.nav.sbl.redis

import kotlinx.coroutines.*
import no.nav.common.health.HealthCheck
import no.nav.common.health.HealthCheckResult
import no.nav.common.health.selftest.SelfTestCheck
import no.nav.sbl.config.Pingable
import java.util.UUID
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RedisPersistence(
    private val redisPool: AuthJedisPool,
    private val codeGenerator: CodeGenerator = UUIDGenerator(),
    private val expiration: Duration = 10.minutes,
    private val scope: String = "fnr-code",
) : HealthCheck, Pingable {

    init {
        fixedRateTimer("Redis check", daemon = true, initialDelay = 0, period = 10.seconds.inWholeMilliseconds) {
            runBlocking(Dispatchers.IO) { ping() }
        }
    }

    suspend fun getFnr(code: String): Result<String?> {
        val key = getKey(code)
        return redisPool.useResource {
            it.get(key)
        }
    }

    suspend fun generateAndStoreTempCodeForFnr(fnr: String): TempCodeResult {
        val code = codeGenerator.generateCode(fnr)
        val key = getKey(code)
        val result = redisPool.useResource {
            it.setex(key, expiration.inWholeSeconds, fnr)
        }
        return TempCodeResult(result, code)
    }

    private fun getKey(code: String): String {
        return "$scope-$code"
    }

    private suspend fun pingRedis(): Result<String?> {
        return redisPool.useResource { it.ping() }
    }

    override fun checkHealth(): HealthCheckResult {
        val result = runBlocking { pingRedis() }
        return if (result.isSuccess) {
            HealthCheckResult.healthy()
        } else {
            HealthCheckResult.unhealthy(result.exceptionOrNull())
        }
    }

    override fun ping(): SelfTestCheck {
        return SelfTestCheck(
            "Redis check",
            false,
            this,
        )
    }
}

data class TempCodeResult(
    val result: Result<String?>,
    val code: String,
)

interface CodeGenerator {
    fun generateCode(fnr: String): String
}

class UUIDGenerator : CodeGenerator {
    override fun generateCode(fnr: String): String {
        return UUID.randomUUID().toString()
    }
}
