package no.nav.sbl.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.common.utils.EnvironmentUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RedisConfig {
    private val redisPort = EnvironmentUtils.getRequiredProperty("REDIS_PORT").toInt()
    private val redisHost = EnvironmentUtils.getRequiredProperty("REDIS_HOST")
    private val redisPassword = EnvironmentUtils.getRequiredProperty("REDIS_PASSWORD")
    private val environment = EnvironmentUtils.getRequiredProperty("APP_ENVIRONMENT_NAME")
    private val channel = "ContextOppdatering-$environment"

    @Bean
    open fun redisPublisher(authJedisPool: AuthJedisPool): RedisPublisher = RedisPublisher(authJedisPool, channel)

    @Bean
    open fun authJedisPool(): AuthJedisPool =
        AuthJedisPool(RedisHostPortAndPassword(host = redisHost, port = redisPort, password = redisPassword))

    @Bean
    open fun redisPersistence(authJedisPool: AuthJedisPool): RedisPersistence = RedisPersistence(authJedisPool)

    @Bean
    open fun redisVeilederContextDatabase(
        authJedisPool: AuthJedisPool,
        objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule()),
    ): RedisVeilederContextDatabase = RedisVeilederContextDatabase(authJedisPool, objectMapper)
}
