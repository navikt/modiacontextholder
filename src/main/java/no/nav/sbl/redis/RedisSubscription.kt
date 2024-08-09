package no.nav.sbl.redis

import redis.clients.jedis.JedisPubSub

data class RedisSubscription(
    val channel: String,
    private val onMessage: (channel: String, message: String) -> Unit,
) {
    val jedisPubSub: JedisPubSub =
        object : JedisPubSub() {
            override fun onMessage(
                channel: String,
                message: String,
            ) {
                onMessage(channel, message)
            }
        }
}
