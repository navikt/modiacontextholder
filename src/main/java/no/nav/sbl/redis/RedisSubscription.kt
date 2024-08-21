package no.nav.sbl.redis

import io.lettuce.core.pubsub.RedisPubSubAdapter

data class RedisSubscription(
    val channel: String,
    private val onMessage: (channel: String, message: String) -> Unit,
) : RedisPubSubAdapter<String, String>() {
    override fun message(
        channel: String,
        message: String,
    ) {
        if (channel != this.channel) return
        onMessage(channel, message)
    }
}
