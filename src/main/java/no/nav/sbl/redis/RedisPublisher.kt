package no.nav.sbl.redis

import no.nav.common.health.HealthCheck
import no.nav.common.health.HealthCheckResult
import no.nav.common.health.selftest.SelfTestCheck
import no.nav.sbl.config.Pingable
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.Jedis

object Redis {
    
    class Publisher(private val hostAndPort: HostAndPort) : HealthCheck, Pingable   {
        private val jedis = Jedis(hostAndPort)
        
        fun publishMessage(channel: String, message: String) {
            jedis.publish(channel, message)
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