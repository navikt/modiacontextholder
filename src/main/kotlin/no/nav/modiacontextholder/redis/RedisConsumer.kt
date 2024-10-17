package no.nav.modiacontextholder.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nav.common.health.HealthCheckResult
import no.nav.common.health.selftest.SelfTestCheck
import no.nav.common.utils.EnvironmentUtils
import no.nav.modiacontextholder.infrastructur.HealthCheckAware
import no.nav.personoversikt.common.utils.SelftestGenerator
import org.slf4j.LoggerFactory
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.minutes

object Redis {
    private val environment = EnvironmentUtils.getOptionalProperty("APP_ENVIRONMENT_NAME").orElse("local")
    private val log = LoggerFactory.getLogger(Redis::class.java)
    private val reporter = SelftestGenerator.Reporter(name = "Redis pubSub consumer", critical = true)

    @JvmStatic
    fun getChannel() = "ContextOppdatering-$environment"

    class Consumer(
        private val redis: RedisClient,
        private val channel: String = getChannel(),
    ) : HealthCheckAware {
        private var running = false
        private var thread: Thread? = null
        private val messageFlow = MutableSharedFlow<String>()

        private var connection: StatefulRedisPubSubConnection<String, String>? = null
        private var redisCommands: RedisPubSubCommands<String, String>? = null

        init {
            fixedRateTimer(name = "Ping", daemon = true, period = 5.minutes.inWholeMilliseconds, initialDelay = 0) {
                checkHealth()
            }
        }

        private val subscriber =
            object : RedisPubSubAdapter<String, String>() {
                override fun message(
                    channel: String?,
                    message: String?,
                ) {
                    runBlocking {
                        try {
                            if (!message.isNullOrEmpty()) {
                                messageFlow.emit(message)
                            }
                        } catch (e: Exception) {
                            log.error(e.message, e)
                        }
                    }
                    log.debug(
                        """
                        Redismelding mottatt på kanal '$channel' med melding:
                        $message
                        """.trimIndent(),
                    )
                }
            }

        fun getFlow() = messageFlow.asSharedFlow()

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
                reporter.reportOk()
                HealthCheckResult
                    .healthy()
            } catch (e: Exception) {
                reporter.reportError(e)
                HealthCheckResult.unhealthy(e)
            }

        override fun getHealthCheck(): SelfTestCheck =
            SelfTestCheck(
                "Redis listener on channel: $channel",
                true,
            ) { this.checkHealth() }
    }
}
