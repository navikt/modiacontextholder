package no.nav.modiacontextholder.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import no.nav.common.health.HealthCheckResult
import no.nav.common.health.selftest.SelfTestCheck
import no.nav.common.utils.EnvironmentUtils
import no.nav.modiacontextholder.infrastructur.HealthCheckAware
import org.slf4j.LoggerFactory

object Redis {
    private val environment = EnvironmentUtils.getOptionalProperty("APP_ENVIRONMENT") ?: "local"
    private val log = LoggerFactory.getLogger(Redis::class.java)

    @JvmStatic
    fun getChannel() = "ContextOppdatering-$environment"

    class Consumer(
        private val redis: RedisClient,
        private val channel: String = getChannel(),
    ) : HealthCheckAware {
        private var running = false
        private var thread: Thread? = null
        private val channelReference = Channel<String?>()

        private var connection: StatefulRedisPubSubConnection<String, String>? = null
        private var redisCommands: RedisPubSubCommands<String, String>? = null

        private val subscriber =
            object : RedisPubSubAdapter<String, String>() {
                override fun message(
                    channel: String?,
                    message: String?,
                ) {
                    runBlocking {
                        try {
                            channelReference.send(message)
                        } catch (e: Exception) {
                            log.error(e.message, e)
                        }
                    }
                    log.info(
                        """
                        Redismelding mottatt på kanal '$channel' med melding:
                        $message
                        """.trimIndent(),
                    )
                }
            }

        fun getFlow() = channelReference.consumeAsFlow()

        fun start() {
            running = true
            log.info("starting redis consumer on channel '$channel'")

            thread = Thread(Runnable { run() })
            thread?.name = "consumer-$channel"
            thread?.isDaemon = true
            thread?.start()
        }

        fun stop() {
            running = false
            connection?.close()
            channelReference.close()
            thread = null
        }

        private fun run() {
            try {
                connection = redis.connectPubSub()
                connection?.addListener(subscriber)

                redisCommands = connection?.sync()
                redisCommands?.subscribe(channel)
            } catch (e: Exception) {
                log.error(e.message, e)
            }
        }

        private fun checkHealth(): HealthCheckResult =
            try {
                redisCommands?.ping()
                HealthCheckResult
                    .healthy()
            } catch (e: Exception) {
                HealthCheckResult.unhealthy(e)
            }

        override fun getHealthCheck(): SelfTestCheck =
            SelfTestCheck(
                "Redis litener on channel: $channel",
                true,
            ) { this.checkHealth() }
    }
}
