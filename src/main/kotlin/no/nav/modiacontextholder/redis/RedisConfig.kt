package no.nav.modiacontextholder.redis

import io.lettuce.core.RedisClient

fun setupRedisConsumer(client: RedisClient): Redis.Consumer =
    Redis.Consumer(
        client,
    )
