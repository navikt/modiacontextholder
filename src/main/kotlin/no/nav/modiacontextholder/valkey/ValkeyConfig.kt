package no.nav.modiacontextholder.valkey

import io.lettuce.core.RedisClient

fun setupValkeyConsumer(client: RedisClient): Valkey.Consumer =
    Valkey.Consumer(
        client,
    )
