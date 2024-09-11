package no.nav.modiacontextholder.redis

import io.lettuce.core.api.sync.RedisStringCommands
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class RedisPersistence(
    private val redis: RedisStringCommands<String, String>,
    private val codeGenerator: CodeGenerator = UUIDGenerator(),
    private val expiration: Duration = 10.minutes,
    private val scope: String = "fnr-code",
) {
    fun getFnr(code: String): Result<String?> =
        runCatching {
            val key = getKey(code)
            redis.get(key)
        }

    fun generateAndStoreTempCodeForFnr(fnr: String): TempCodeResult {
        val code = codeGenerator.generateCode(fnr)
        val key = getKey(code)
        val result =
            runCatching {
                redis.setex(key, expiration.inWholeSeconds, fnr)
            }
        return TempCodeResult(result, code)
    }

    private fun getKey(code: String): String = "$scope-$code"
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
