package no.nav.sbl.redis

import redis.clients.jedis.JedisPubSub

data class RedisSubscription(
    val channel: String,
    private val onMessageHandler: (channel: String, message: String) -> Unit,
) : JedisPubSub() {
    override fun onMessage(
        channel: String,
        message: String,
    ) {
        onMessageHandler(channel, message)
    }
}
