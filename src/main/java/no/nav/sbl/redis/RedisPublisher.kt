package no.nav.sbl.redis

import no.nav.common.health.HealthCheck
import no.nav.common.health.HealthCheckResult
import no.nav.common.health.selftest.SelfTestCheck
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.config.Pingable
import org.slf4j.LoggerFactory
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.Jedis

object Redis {
    private val environment = EnvironmentUtils.getRequiredProperty("APP_ENVIRONMENT_NAME")
    
    @JvmStatic
    fun getChannel() = "ContextOppdatering-$environment"
    
    class Publisher(private val hostAndPort: HostAndPort) : HealthCheck, Pingable {
        private val logger = LoggerFactory.getLogger(Publisher::class.java)
        private val jedis = Jedis(hostAndPort)
        
        fun publishMessage(channel: String, message: String) {
            jedis.publish(channel, message)
            logger.info(
                """
                Redismelding sendt på kanal '$channel' med melding:
                $message
                """.trimIndent()
            )
        }
    
        override fun checkHealth(): HealthCheckResult {
            return try {
                jedis.ping()
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