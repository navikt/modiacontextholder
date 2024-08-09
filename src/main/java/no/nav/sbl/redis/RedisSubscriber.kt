package no.nav.sbl.redis

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.context.Lifecycle

class RedisSubscriber(
    private val authJedisPool: AuthJedisPool,
    private val redisSubscriptions: List<RedisSubscription>,
) : Lifecycle {
    private lateinit var job: Job

    private suspend fun subscribe() {
        authJedisPool.useResource {
            redisSubscriptions.forEach {
                it.jedisPubSub.subscribe(it.channel)
            }
        }
    }

    private fun unsubscribe() {
        redisSubscriptions.forEach {
            it.jedisPubSub.unsubscribe()
        }
    }

    override fun start() {
        job =
            CoroutineScope(Dispatchers.IO).launch {
                subscribe()
            }
    }

    override fun stop() {
        unsubscribe()
        if (::job.isInitialized) {
            job.cancel()
        }
    }

    override fun isRunning(): Boolean = ::job.isInitialized && job.isActive
}
