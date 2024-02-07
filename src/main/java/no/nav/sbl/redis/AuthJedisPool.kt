package no.nav.sbl.redis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool

class AuthJedisPool(private val redisHostPortAndPassword: RedisHostPortAndPassword) {
    private val log = LoggerFactory.getLogger(AuthJedisPool::class.java)
    private val pool = JedisPool(redisHostPortAndPassword.host, redisHostPortAndPassword.port)

    suspend fun <T> useResource(block: (Jedis) -> T): Result<T?> {
        return withContext(Dispatchers.IO) {
            if (pool.isClosed) {
                log.error("JedisPool is closed while trying to access it")
                Result.failure(IllegalStateException("RedisPool is closed"))
            } else {
                runCatching {
                    pool.resource.use {
                        it.auth(redisHostPortAndPassword.password)
                        block(it)
                    }
                }
            }
        }.onFailure {
            log.error("Redis-error", it)
        }
    }
}

data class RedisHostPortAndPassword(
    val host: String,
    val port: Int,
    val password: String,
)
