package no.nav.modiacontextholder.redis

import no.nav.common.utils.EnvironmentUtils

fun setupRedis(): Redis.Consumer =
    Redis.Consumer(
        uri = EnvironmentUtils.getRequiredProperty("REDIS_URI_MODIACONTEXTHOLDER"),
        user = EnvironmentUtils.getRequiredProperty("REDIS_USERNAME_MODIACONTEXTHOLDER"),
        password = EnvironmentUtils.getRequiredProperty("REDIS_PASSWORD_MODIACONTEXTHOLDER"),
    )
