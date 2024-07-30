package no.nav.sbl.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.common.utils.EnvironmentUtils
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
    open fun authJedisPool(): AuthJedisPool = AuthJedisPool(RedisUriWithAuth(uri = redisUri, user = redisUser, password = redisPassword))

    @Bean
    open fun redisPersistence(authJedisPool: AuthJedisPool): RedisPersistence = RedisPersistence(authJedisPool)

    @Bean
    open fun veilederContextDatabase(
        authJedisPool: AuthJedisPool,
        objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule()),
    ): VeilederContextDatabase = RedisVeilederContextDatabase(authJedisPool, objectMapper)
}
