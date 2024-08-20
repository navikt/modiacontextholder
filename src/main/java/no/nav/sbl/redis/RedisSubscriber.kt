package no.nav.sbl.redis

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import no.nav.common.health.HealthCheck
import no.nav.common.health.HealthCheckResult
import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPooled

class RedisSubscriber(
    private val jedisPooled: JedisPooled,
    private val redisSubscriptions: List<RedisSubscription>,
) : HealthCheck {
    private val log = LoggerFactory.getLogger(RedisSubscriber::class.java)
    private var healthCheck: HealthCheckResult = HealthCheckResult.healthy()
    private val exceptionHandler =
        CoroutineExceptionHandler { _, exception ->
            log.error("Feil med RedisSubscriber", exception)
            healthCheck = HealthCheckResult.unhealthy("Feil med RedisSubscriber", exception)
        }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)

    @PostConstruct
    fun subscribe() =
        scope.launch {
            redisSubscriptions
                .map { subscription ->
                    launch {
                        jedisPooled.subscribe(subscription, subscription.channel)
                        log.info("Satt opp Redis-abonnement på kanal ${subscription.channel}")
                    }
                }.joinAll()
        }

    @PreDestroy
    fun unsubscribe() {
        redisSubscriptions.forEach {
            it.unsubscribe()
            log.info("Avsluttet Redis-abonnement på kanal ${it.channel}")
        }
    }

    override fun checkHealth(): HealthCheckResult = healthCheck
}
