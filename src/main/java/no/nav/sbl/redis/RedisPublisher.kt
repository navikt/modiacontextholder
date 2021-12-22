package no.nav.sbl.redis

import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.config.ApplicationConfig
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.Jedis


object Redis {
    data class Message(val message: String, val channel: String)
    
    class Publisher(host: String, port: Int)  {
        
        val jedis = Jedis(HostAndPort(host, port), setupConfig(true))
        
        fun publishMessage(redisMessage: Message) {
            jedis.publish(redisMessage.channel, redisMessage.message)
        }
        
    }
    
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
    
    // TODO: Håndtere verdier for host og port
    @JvmStatic
    fun createPublisher() = Publisher("localhost", 6379)
    
    @JvmStatic
    fun createMessage(topic: String, veilederIdent: String, eventJson: String): Message {
        // TODO("Lag correct mapping til en melding")
        return Message(
            message = eventJson,
            channel = topic
        )
    }
    
}