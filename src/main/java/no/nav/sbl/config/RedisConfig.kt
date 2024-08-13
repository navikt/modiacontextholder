package no.nav.sbl.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.redis.RedisPersistence
import no.nav.sbl.redis.RedisPublisher
import no.nav.sbl.redis.RedisSubscriber
import no.nav.sbl.redis.RedisSubscription
import no.nav.sbl.redis.RedisVeilederContextDatabase
import no.nav.sbl.redis.VeilederContextDatabase
import no.nav.sbl.rest.model.RSEvent
import no.nav.sbl.websocket.WebSocketContextEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisPool
import kotlin.jvm.optionals.getOrDefault

@Configuration
open class RedisConfig {
    private val redisUri = EnvironmentUtils.getRequiredProperty("REDIS_URI").removePrefix("rediss://")
    private val redisUser = EnvironmentUtils.getOptionalProperty("REDIS_USER").getOrDefault("default")
    private val redisPassword = EnvironmentUtils.getRequiredProperty("REDIS_PASSWORD")
    private val environment = EnvironmentUtils.getRequiredProperty("APP_ENVIRONMENT_NAME")
    private val channel = "ContextOppdatering-$environment"

    @Bean
    open fun redisPublisher(jedisPool: JedisPool): RedisPublisher = RedisPublisher(jedisPool, channel)

    @Bean
    open fun redisSubscriber(
        jedisPool: JedisPool,
        redisSubscriptions: List<RedisSubscription>,
    ): RedisSubscriber = RedisSubscriber(jedisPool, redisSubscriptions)

    @Bean
    open fun jedisPooled(): JedisPool {
        val hostAndPort = HostAndPort.from(redisUri)
        val jedisClientConfig =
            DefaultJedisClientConfig
                .builder()
                .user(redisUser)
                .password(redisPassword)
                .timeoutMillis(0)
                .build()
        return JedisPool(hostAndPort, jedisClientConfig)
    }

    @Bean
    open fun redisPersistence(jedisPool: JedisPool): RedisPersistence = RedisPersistence(jedisPool)

    @Bean
    open fun veilederContextDatabase(
        jedisPool: JedisPool,
        objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule()),
    ): VeilederContextDatabase = RedisVeilederContextDatabase(jedisPool, objectMapper)

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
