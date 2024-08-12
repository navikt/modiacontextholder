package no.nav.sbl.redis

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import redis.clients.jedis.JedisPooled

class RedisSubscriber(
    private val jedisPooled: JedisPooled,
    private val redisSubscriptions: List<RedisSubscription>,
) : SmartLifecycle {
    private val log = LoggerFactory.getLogger(RedisSubscriber::class.java)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private suspend fun subscribe() =
        coroutineScope {
            redisSubscriptions.forEach {
                launch {
                    try {
                        jedisPooled.subscribe(it.jedisPubSub, it.channel)
                    } finally {
                        it.jedisPubSub.unsubscribe()
                    }
                }
            }
        }

    private fun unsubscribe() {
        redisSubscriptions.forEach {
            it.jedisPubSub.unsubscribe()
        }
    }

    override fun start() {
        scope.launch {
            subscribe()
        }
    }

    override fun stop() {
        log.info("Stopper RedisSubscriber")
        unsubscribe()
        scope.cancel()
    }

    override fun isRunning(): Boolean = scope.isActive

    override fun isAutoStartup(): Boolean = true

    override fun getPhase(): Int = 0
}
