package no.nav.sbl.redis

import kotlinx.coroutines.runBlocking
import no.nav.common.json.JsonUtils
import no.nav.sbl.service.ContextEventPublisher
import no.nav.sbl.rest.model.RSEvent
import org.slf4j.LoggerFactory

class RedisPublisher(
    private val authJedisPool: AuthJedisPool,
    private val channel: String,
) : ContextEventPublisher {
    private val logger = LoggerFactory.getLogger(RedisPublisher::class.java)

    fun publishMessage(message: String) {
        val result =
            runBlocking {
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

    override fun publishMessage(
        ident: String,
        eventType: String,
    ) {
        publishMessage(JsonUtils.toJson(RSEvent(ident, eventType)))
    }
}
