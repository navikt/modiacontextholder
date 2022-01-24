package no.nav.sbl.redis

import no.nav.common.health.HealthCheck
import no.nav.common.health.HealthCheckResult
import no.nav.common.health.selftest.SelfTestCheck
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.config.Pingable
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.slf4j.LoggerFactory
import redis.clients.jedis.*
import redis.clients.jedis.util.Pool

object Redis {
    private val environment = EnvironmentUtils.getRequiredProperty("APP_ENVIRONMENT_NAME")
    
    @JvmStatic
    fun getChannel() = "ContextOppdatering-$environment"
    
    class Publisher(private val hostAndPort: HostAndPort) : HealthCheck, Pingable {
        private val logger = LoggerFactory.getLogger(Publisher::class.java)
        private val poolConfig = GenericObjectPoolConfig<Jedis>().apply {
            minIdle = 1
            maxIdle = 4
            maxWaitMillis = 1000
        }
        private val jedisPool = JedisPool(poolConfig, hostAndPort, DefaultJedisClientConfig.builder().build())

        fun publishMessage(channel: String, message: String) {
            jedisPool.withResource {
                publish(channel, message)
            }
            logger.info(
                """
                Redismelding sendt på kanal '$channel' med melding:
                $message
                """.trimIndent()
            )
        }
    
        override fun checkHealth(): HealthCheckResult {
            return try {
                jedisPool.withResource {
                    ping()
                }
                HealthCheckResult.healthy()
            } catch (e: Exception) {
                HealthCheckResult.unhealthy(e)
            }
        }
    
        override fun ping(): SelfTestCheck {
            return SelfTestCheck(
                "Redis - via ${hostAndPort.host}",
                false,
                this
            )
        }
    }
}
private fun <RESOURCE> Pool<RESOURCE>.withResource(block: RESOURCE.() -> Unit) {
    val resource: RESOURCE = this.resource
    runCatching {
        block(resource)
    }.onSuccess {
        returnResource(resource)
    }.onFailure {
        returnBrokenResource(resource)
    }
}