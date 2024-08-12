package no.nav.sbl.redis

import org.slf4j.LoggerFactory
import redis.clients.jedis.JedisPool

class RedisPublisher(
    private val jedisPool: JedisPool,
    private val channel: String,
) {
    private val logger = LoggerFactory.getLogger(RedisPublisher::class.java)

    fun publishMessage(message: String) {
        jedisPool.resource.use {
            it.publish(channel, message)
            logger.info(
                """
                Redismelding sendt på kanal '$channel' med melding:
                $message
                """.trimIndent(),
            )
        }
    }
}
