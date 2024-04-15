package no.nav.sbl.redis

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import no.nav.sbl.redis.TestUtils.WithRedis.Companion.PASSWORD
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class RedisPersistenceTest : TestUtils.WithRedis() {
    private val hostAndPort = redisHostAndPort()
    private val authJedisPool = AuthJedisPool(
        redisHostPortAndPassword = RedisHostPortAndPassword(
            host = hostAndPort.host,
            port = hostAndPort.port,
            password = PASSWORD,
        ),
    )

    private var redisPersistence: RedisPersistence = RedisPersistence(authJedisPool)

    @Test
    fun `lager kode for fnr`() {
        val fnr = "10108000398"
        val fnrCodeResult = runBlocking { redisPersistence.generateAndStoreTempCodeForFnr(fnr) }

        assertTrue(fnrCodeResult.result.isSuccess)
        assertNotNull(fnrCodeResult.code)

        val result = runBlocking { redisPersistence.getFnr(fnrCodeResult.code) }

        assertEquals(fnr, result.getOrNull())
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `sletter fnr etter gitt tid`() {
        redisPersistence = RedisPersistence(authJedisPool, expiration = 1.seconds)
        val fnr = "10108000398"
        val fnrCodeResult = runBlocking { redisPersistence.generateAndStoreTempCodeForFnr(fnr) }
        assertTrue(fnrCodeResult.result.isSuccess)

        runBlocking { delay(2.seconds) }

        val result = runBlocking { redisPersistence.getFnr(fnrCodeResult.code) }
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }
}
