package no.nav.sbl.redis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool

class AuthJedisPool(
    private val uriWithAuth: RedisUriWithAuth,
) {
    private val log = LoggerFactory.getLogger(AuthJedisPool::class.java)
    private val pool = JedisPool(uriWithAuth.uri)

    suspend fun <T> useResource(block: suspend (Jedis) -> T): Result<T?> =
        withContext(Dispatchers.IO) {
            if (pool.isClosed) {
                log.error("JedisPool is closed while trying to access it")
                Result.failure(IllegalStateException("RedisPool is closed"))
            } else {
                runCatching {
                    pool.resource.use {
                        it.auth(uriWithAuth.user, uriWithAuth.password)
                        block(it)
                    }
                }
            }
        }.onFailure {
            log.error("Redis-error", it)
        }
}

data class RedisUriWithAuth(
    val user: String,
    val uri: String,
    val password: String,
)
