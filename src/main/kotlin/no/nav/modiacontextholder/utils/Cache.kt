package no.nav.modiacontextholder.utils

import com.github.benmanes.caffeine.cache.Caffeine
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration
import com.github.benmanes.caffeine.cache.Cache as CaffeineCache

object CacheFactory {
    fun <KEY: Any, VALUE> createCache(
        expiry: Duration = 1.hours,
        maximumSize: Long = 10_000,
    ) = LocalCache<KEY, VALUE>(
        Caffeine
            .newBuilder()
            .expireAfterWrite(expiry.toJavaDuration())
            .maximumSize(maximumSize)
            .build(),
    )

    fun <KEY : Any, VALUE> createDistributedCache(
        expiry: Duration = 1.hours,
        name: String = "cache-${UUID.randomUUID()}",
        serializer: KSerializer<VALUE>,
    ): DistributedCache<KEY, VALUE> = DistributedCache(expiry, name, serializer)
}

interface Cache<KEY, VALUE> {
    suspend fun get(
        key: KEY,
        block: (KEY) -> VALUE,
    ): VALUE

    suspend fun put(
        key: KEY,
        value: VALUE & Any,
    )

    suspend fun invalidate(key: KEY)
}

class LocalCache<K: Any, V>(
    private val cacheDelegate: CaffeineCache<K, V>,
) : Cache<K, V> {
    override suspend fun get(
        key: K,
        block: (K) -> V,
    ): V =
        cacheDelegate.get(key) {
            block.invoke(it)
        }

    override suspend fun put(
        key: K,
        value: V & Any,
    ) {
        cacheDelegate.put(key, value)
    }

    override suspend fun invalidate(key: K) {
        cacheDelegate.invalidate(key)
    }
}

class DistributedCache<K : Any, V>(
    private val expiry: Duration,
    val cacheName: String,
    private val serializer: KSerializer<V>,
) : KoinComponent,
    Cache<K, V> {
    private val redisClient: RedisClient by inject(qualifier = named("cache"))

    @ExperimentalLettuceCoroutinesApi
    val connection = redisClient.connect().coroutines()

    private fun <K> encodeKey(key: K) = "$cacheName:$key"

    private fun encodeValue(value: V): String = Json.encodeToString(serializer, value)

    private fun decodeValue(value: String): V = Json.decodeFromString(serializer, value)

    override suspend fun get(
        key: K,
        block: (K) -> V,
    ): V =
        @OptIn(ExperimentalLettuceCoroutinesApi::class)
        connection.get(encodeKey(key))?.let { decodeValue(it) }
            ?: run {
                val value = block.invoke(key)
                if (value === null || value is List<*> && value.isEmpty()) {
                    return value
                }

                val stringValue = encodeValue(value)
                @OptIn(ExperimentalLettuceCoroutinesApi::class)
                connection.setex(encodeKey(key), expiry.inWholeSeconds, stringValue)

                value
            }

    override suspend fun put(
        key: K,
        value: V & Any,
    ) {
        @OptIn(ExperimentalLettuceCoroutinesApi::class)
        connection.setex(encodeKey(key), expiry.inWholeSeconds, encodeValue(value))
    }

    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    override suspend fun invalidate(key: K) {
        connection.del(encodeKey(key))
    }
}
