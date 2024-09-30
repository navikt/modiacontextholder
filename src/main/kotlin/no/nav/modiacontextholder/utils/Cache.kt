package no.nav.modiacontextholder.utils

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

object CacheFactory {
    fun <KEY, VALUE> createCache(
        expiry: Duration = 1.hours,
        maximumSize: Long = 10_000,
    ): Cache<KEY, VALUE> =
        Caffeine
            .newBuilder()
            .expireAfterWrite(expiry.toJavaDuration())
            .maximumSize(maximumSize)
            .build()
}
