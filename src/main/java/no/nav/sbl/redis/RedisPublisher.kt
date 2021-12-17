package no.nav.sbl.redis

import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.config.ApplicationConfig
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.Jedis

class RedisPublisher(host: String, port: Int)  {
    
    val jedis = Jedis(HostAndPort(host, port), RedisConfig.setupConfig(true))
    
    fun publishMessage(channel: String, message: String) {
        jedis.publish(channel, message)
    }
    
}

class RedisConfig {
    companion object {
        @JvmStatic
        fun setupConfig(isUsingSaslSsl: Boolean): DefaultJedisClientConfig {
            val configBuilder = DefaultJedisClientConfig.builder()
            val username = EnvironmentUtils.getRequiredProperty(ApplicationConfig.SRV_USERNAME_PROPERTY)
            val password = EnvironmentUtils.getRequiredProperty(ApplicationConfig.SRV_PASSWORD_PROPERTY)
            configBuilder
                .user(username)
                .password(password)
                .ssl(isUsingSaslSsl)
            return configBuilder.build()
        }
    }
}