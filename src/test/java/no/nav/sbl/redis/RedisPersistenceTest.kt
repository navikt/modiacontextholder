package no.nav.sbl.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.time.Duration.Companion.seconds

class RedisPersistenceTest : TestUtils.WithRedis() {
    private val hostAndPort = redisHostAndPort()
    private val redisConnection =
        RedisClient
            .create(
                RedisURI
                    .builder(RedisURI.create("redis://$hostAndPort"))
                    .withAuthentication("default", PASSWORD)
                    .build(),
            ).connect()
    private val redis = redisConnection.sync()

    private val redisPersistence: RedisPersistence = RedisPersistence(redis)

    @Test
    fun `lager kode for fnr`() {
        val fnr = "10108000398"
        val fnrCodeResult = runBlocking { redisPersistence.generateAndStoreTempCodeForFnr(fnr) }

        assertTrue(fnrCodeResult.result.isSuccess)
        assertNotNull(fnrCodeResult.code)

        val result = runBlocking { redisPersistence.getFnr(fnrCodeResult.code) }

        assertEquals(fnr, result.getOrNull())
    }

    @Test
    fun `sletter fnr etter gitt tid`() {
        val localRedisPersitence = RedisPersistence(redis, expiration = 1.seconds)
        val fnr = "10108000398"
        val fnrCodeResult = runBlocking { localRedisPersitence.generateAndStoreTempCodeForFnr(fnr) }
        assertTrue(fnrCodeResult.result.isSuccess)

        runBlocking { delay(2.seconds) }

        val result = runBlocking { localRedisPersitence.getFnr(fnrCodeResult.code) }
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }
}
