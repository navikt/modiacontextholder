package no.nav.sbl.redis

import org.slf4j.LoggerFactory

class RedisPublisher(private val authJedisPool: AuthJedisPool, private val channel: String) {
    private val logger = LoggerFactory.getLogger(RedisPublisher::class.java)

    fun publishMessage(message: String) {
        println("HALLO")
        return
//        return runBlocking {
//            authJedisPool.useResource {
//                it.publish(channel, message)
//                logger.info(
//                    """
//                Redismelding sendt på kanal '$channel' med melding:
//                $message
//                    """.trimIndent(),
//                )
//            }
//        }
    }
}
