package no.nav.sbl.redis

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
}
