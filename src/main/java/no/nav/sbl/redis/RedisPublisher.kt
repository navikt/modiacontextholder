package no.nav.sbl.redis

import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands
import org.slf4j.LoggerFactory

class RedisPublisher(
    private val redis: RedisPubSubCommands<String, String>,
    private val channel: String,
) {
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
