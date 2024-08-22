package no.nav.sbl.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.RedisPubSubListener
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.redis.RedisPersistence
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.redis.RedisSubscription
import no.nav.sbl.redis.RedisVeilederContextDatabase
import no.nav.sbl.redis.VeilederContextDatabase
import no.nav.sbl.rest.model.RSEvent
import no.nav.sbl.websocket.WebSocketContextEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
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
    open fun redisClient(): RedisClient =
        RedisClient.create(
            RedisURI
                .builder(RedisURI.create(redisUri))
                .withAuthentication(redisUser, redisPassword)
                .build(),
        )

    @Bean
    open fun statefulRedisConnection(redisClient: RedisClient): StatefulRedisConnection<String, String> = redisClient.connect()

    @Bean
    open fun statefulRedisPubSubConnection(
        redisClient: RedisClient,
        redisPubSubListeners: List<RedisPubSubListener<String, String>>,
    ): StatefulRedisPubSubConnection<String, String> =
        redisClient.connectPubSub().apply {
            redisPubSubListeners.forEach(::addListener)
        }

    @Bean("redisCommands")
    open fun redisCommands(statefulRedisConnection: StatefulRedisConnection<String, String>): RedisCommands<String, String> =
        statefulRedisConnection.sync()

    @Bean("redisPubSubCommands")
    open fun redisPubSubCommands(
        statefulRedisPubSubConnection: StatefulRedisPubSubConnection<String, String>,
        redisPubSubListeners: List<RedisSubscription>,
    ): RedisPubSubCommands<String, String> =
        statefulRedisPubSubConnection.sync().apply {
            redisPubSubListeners.forEach {
                this.subscribe(it.channel)
            }
        }

    @Bean
    open fun redisPublisher(redis: RedisPubSubCommands<String, String>): RedisPublisher = RedisPublisher(redis, channel)

    @Bean
    open fun redisPersistence(
        @Qualifier("redisCommands") redis: RedisCommands<String, String>,
    ): RedisPersistence = RedisPersistence(redis)

    @Bean
    open fun veilederContextDatabase(
        @Qualifier("redisCommands") redis: RedisCommands<String, String>,
        objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule()),
    ): VeilederContextDatabase = RedisVeilederContextDatabase(redis, objectMapper)

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
