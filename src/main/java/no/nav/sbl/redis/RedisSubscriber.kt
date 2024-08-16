package no.nav.sbl.redis

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.nav.common.health.HealthCheck
import no.nav.common.health.HealthCheckResult
import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import redis.clients.jedis.JedisPool

class RedisSubscriber(
    private val jedisPool: JedisPool,
    private val redisSubscriptions: List<RedisSubscription>,
) : SmartLifecycle,
    HealthCheck {
    private val log = LoggerFactory.getLogger(RedisSubscriber::class.java)
    private var healthCheck: HealthCheckResult = HealthCheckResult.healthy()
    private val exceptionHandler =
        CoroutineExceptionHandler { _, exception ->
            log.error("Feil med RedisSubscriber", exception)
            healthCheck = HealthCheckResult.unhealthy("Feil med RedisSubscriber", exception)
        }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)

    private suspend fun subscribe() =
        coroutineScope {
            redisSubscriptions.forEach { subscription ->
                launch {
                    try {
                        jedisPool.resource.use { jedis ->
                            log.info("Starter å lytte på kanal ${subscription.channel}")
                            jedis.subscribe(subscription.jedisPubSub, subscription.channel)
                        }
                    } finally {
                        log.info("Avslutter å lytte på kanal ${subscription.channel}")
                        subscription.jedisPubSub.unsubscribe()
                    }
                }
            }
        }

    override fun start() {
        log.info("Starter RedisSubscriber")
        scope.launch {
            subscribe()
        }
    }

    override fun stop() {
        log.info("Stopper RedisSubscriber")
        scope.cancel()
    }

    override fun isRunning(): Boolean = scope.isActive

    override fun isAutoStartup(): Boolean = true

    override fun getPhase(): Int = 0

    override fun checkHealth(): HealthCheckResult = healthCheck
}
