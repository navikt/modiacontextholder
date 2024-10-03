package no.nav.modiacontextholder.redis

import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import no.nav.common.utils.EnvironmentUtils
import no.nav.personoversikt.common.utils.SelftestGenerator
import org.slf4j.LoggerFactory
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RedisPublisher(
    redisConnection: StatefulRedisPubSubConnection<String, String>,
    private val channel: String = getChannel(),
) {
    companion object {
        private val environment = EnvironmentUtils.getOptionalProperty("APP_ENVIRONMENT_NAME").orElse("local")

        @JvmStatic
        fun getChannel() = "ContextOppdatering-$environment"
    }

    private val redis = redisConnection.sync()

    private val logger = LoggerFactory.getLogger(RedisPublisher::class.java)
    private val reporter = SelftestGenerator.Reporter(name = "Redis publisher", critical = true)

    init {
        fixedRateTimer(
            name = "Redis publisher health",
            daemon = false,
            period = 1.minutes.inWholeMilliseconds,
            initialDelay = 1.seconds.inWholeMilliseconds,
        ) {
            runCatching { redis.ping() }
                .onFailure { reporter.reportError(it) }
                .onSuccess { reporter.reportOk() }
        }
    }

    fun publishMessage(message: String) {
        logger.info(
            """
            Redismelding sendes på kanal '$channel' med melding:
            $message
            """.trimIndent(),
        )
        redis.publish(channel, message)
    }
}
