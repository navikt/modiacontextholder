package no.nav.sbl.redis

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class RedisPublisher(private val authJedisPool: AuthJedisPool, private val channel: String) {
    private val logger = LoggerFactory.getLogger(RedisPublisher::class.java)

    fun publishMessage(message: String) {
        val result = runBlocking {
            authJedisPool.useResource {
                it.publish(channel, message)
                logger.info(
                    """
                Redismelding sendt på kanal '$channel' med melding:
                $message
                    """.trimIndent(),
                )
            }
        }
        if (result.isFailure) {
            throw result.exceptionOrNull() ?: Throwable("Unkown exception when publishing message")
        }
    }
}
