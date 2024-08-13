package no.nav.sbl.redis

import no.nav.common.health.HealthCheck
import no.nav.common.health.HealthCheckResult
import no.nav.common.health.selftest.SelfTestCheck
import no.nav.sbl.config.Pingable
import redis.clients.jedis.JedisPool
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class RedisPersistence(
    private val jedisPool: JedisPool,
    private val codeGenerator: CodeGenerator = UUIDGenerator(),
    private val expiration: Duration = 10.minutes,
    private val scope: String = "fnr-code",
) : HealthCheck,
    Pingable {
    fun getFnr(code: String): Result<String?> =
        runCatching {
            val key = getKey(code)
            jedisPool.resource.use { jedis -> jedis.get(key) }
        }

    fun generateAndStoreTempCodeForFnr(fnr: String): TempCodeResult {
        val code = codeGenerator.generateCode(fnr)
        val key = getKey(code)
        val result =
            runCatching {
                jedisPool.resource.use { jedis -> jedis.setex(key, expiration.inWholeSeconds, fnr) }
            }
        return TempCodeResult(result, code)
    }

    private fun getKey(code: String): String = "$scope-$code"

    private fun pingRedis(): Result<String?> =
        runCatching {
            jedisPool.resource.use { jedis ->
                val pong = jedis.ping()
                if (pong != "PONG") {
                    throw IllegalStateException("Redis ping feilet")
                }
                pong
            }
        }

    override fun checkHealth(): HealthCheckResult {
        val result = pingRedis()
        return if (result.isSuccess) {
            HealthCheckResult.healthy()
        } else {
            HealthCheckResult.unhealthy(result.exceptionOrNull())
        }
    }

    override fun ping(): SelfTestCheck =
        SelfTestCheck(
            "Redis check",
            false,
            this,
        )
}

data class TempCodeResult(
    val result: Result<String?>,
    val code: String,
)

interface CodeGenerator {
    fun generateCode(fnr: String): String
}

class UUIDGenerator : CodeGenerator {
    override fun generateCode(fnr: String): String = UUID.randomUUID().toString()
}
