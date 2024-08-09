package no.nav.sbl.redis

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.Lifecycle

class RedisSubscriber(
    private val authJedisPool: AuthJedisPool,
    private val redisSubscriptions: List<RedisSubscription>,
) : Lifecycle {
    private val log = LoggerFactory.getLogger(RedisSubscriber::class.java)
    private lateinit var job: Job

    private suspend fun subscribe() {
        authJedisPool.useResource { jedis ->
            redisSubscriptions.forEach {
                jedis.subscribe(it.jedisPubSub, it.channel)
            }
        }
    }

    private fun unsubscribe() {
        redisSubscriptions.forEach {
            it.jedisPubSub.unsubscribe()
        }
    }

    override fun start() {
        if (::job.isInitialized) {
            log.warn("RedisSubscriber er allerede startet")
            return
        }
        log.info("Starter RedisSubscriber")
        job =
            CoroutineScope(Dispatchers.IO).launch {
                subscribe()
            }
    }

    override fun stop() {
        log.info("Stopper RedisSubscriber")
        unsubscribe()
        if (::job.isInitialized) {
            job.cancel()
        }
    }

    override fun isRunning(): Boolean = ::job.isInitialized && job.isActive
}
