package no.nav.sbl.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.redis.AuthJedisPool
import no.nav.sbl.redis.RedisPersistence
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.redis.RedisSubscriber
import no.nav.sbl.redis.RedisSubscription
import no.nav.sbl.redis.RedisUriWithAuth
import no.nav.sbl.redis.RedisVeilederContextDatabase
import no.nav.sbl.redis.VeilederContextDatabase
import no.nav.sbl.rest.model.RSEvent
import no.nav.sbl.websocket.WebSocketContextEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.jvm.optionals.getOrDefault

@Configuration
open class RedisConfig {
    private val redisUri = EnvironmentUtils.getRequiredProperty("REDIS_URI")
    private val redisUser = EnvironmentUtils.getOptionalProperty("REDIS_USER").getOrDefault("default")
    private val redisPassword = EnvironmentUtils.getRequiredProperty("REDIS_PASSWORD")
    private val environment = EnvironmentUtils.getRequiredProperty("APP_ENVIRONMENT_NAME")
    private val channel = "ContextOppdatering-$environment"

    @Bean
    open fun redisPublisher(authJedisPool: AuthJedisPool): RedisPublisher = RedisPublisher(authJedisPool, channel)

    @Bean
    open fun redisSubscriber(
        authJedisPool: AuthJedisPool,
        redisSubscriptions: List<RedisSubscription>,
    ): RedisSubscriber = RedisSubscriber(authJedisPool, redisSubscriptions)

    @Bean
    open fun authJedisPool(): AuthJedisPool = AuthJedisPool(RedisUriWithAuth(uri = redisUri, user = redisUser, password = redisPassword))

    @Bean
    open fun redisPersistence(authJedisPool: AuthJedisPool): RedisPersistence = RedisPersistence(authJedisPool)

    @Bean
    open fun veilederContextDatabase(
        authJedisPool: AuthJedisPool,
        objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule()),
    ): VeilederContextDatabase = RedisVeilederContextDatabase(authJedisPool, objectMapper)

    @Bean
    open fun contextEventRedisSubscription(
        contextEventPublisher: WebSocketContextEventPublisher,
        objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule()),
    ): RedisSubscription {
        val log = LoggerFactory.getLogger(RedisSubscription::class.java)

        return RedisSubscription(
            channel,
        ) { channel, message ->
            runCatching {
                objectMapper.readValue(message, RSEvent::class.java)
            }.fold(
                onSuccess = { contextEventPublisher.publishMessage(it.veilederIdent, it.eventType) },
                onFailure = { log.error("Feil ved deserialisering av Redis-melding på kanal $channel", it) },
            )
        }
    }
}
