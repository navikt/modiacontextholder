package no.nav.sbl.redis

import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPooled

class RedisPublisher(
    private val jedisPooled: JedisPooled,
    private val channel: String,
) {
    private val logger = LoggerFactory.getLogger(RedisPublisher::class.java)

    fun publishMessage(message: String) {
        jedisPooled.publish(channel, message)
        logger.info(
            """
            Redismelding sendt på kanal '$channel' med melding:
            $message
            """.trimIndent(),
        )
    }
}
