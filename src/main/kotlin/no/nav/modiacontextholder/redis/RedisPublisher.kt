package no.nav.modiacontextholder.redis

import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import no.nav.common.utils.EnvironmentUtils
import org.slf4j.LoggerFactory

class RedisPublisher(
    redisConnection: StatefulRedisPubSubConnection<String, String>,
    private val channel: String = getChannel(),
) {
    companion object {
        private val environment = EnvironmentUtils.getOptionalProperty("APP_ENVIRONMENT") ?: "local"

        @JvmStatic
        fun getChannel() = "ContextOppdatering-$environment"
    }

    private val redis = redisConnection.sync()

    private val logger = LoggerFactory.getLogger(RedisPublisher::class.java)

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
