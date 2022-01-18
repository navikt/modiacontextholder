package no.nav.sbl.redis

import no.nav.common.utils.EnvironmentUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import redis.clients.jedis.HostAndPort

@Configuration
open class RedisConfig {
    @Bean
    open fun redisPublisher(): Redis.Publisher {
        return Redis.Publisher(HostAndPort(EnvironmentUtils.getRequiredProperty("REDIS_HOST"), 6379))
    }
}