package no.nav.modiacontextholder.valkey

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class ValkeyPersistenceTest : TestUtils.WithValkey() {
    private val hostAndPort = redisHostAndPort()
    private val valkeyConnection =
        RedisClient
            .create(
                RedisURI
                    .builder(RedisURI.create("redis://$hostAndPort"))
                    .withAuthentication("default", PASSWORD)
                    .build(),
            ).connect()

    private val valkeyPersistence: ValkeyPersistence = ValkeyPersistence(valkeyConnection)

    @Test
    fun `lager kode for fnr`() {
        val fnr = "10108000398"
        val fnrCodeResult = runBlocking { valkeyPersistence.generateAndStoreTempCodeForFnr(fnr) }

        assertTrue(fnrCodeResult.result.isSuccess)
        assertNotNull(fnrCodeResult.code)

        val result = runBlocking { valkeyPersistence.getFnr(fnrCodeResult.code) }

        assertEquals(fnr, result.getOrNull())
    }

    @Test
    fun `sletter fnr etter gitt tid`() {
        val localValkeyPersitence = ValkeyPersistence(valkeyConnection, expiration = 1.seconds)
        val fnr = "10108000398"
        val fnrCodeResult = runBlocking { localValkeyPersitence.generateAndStoreTempCodeForFnr(fnr) }
        assertTrue(fnrCodeResult.result.isSuccess)

        runBlocking { delay(2.seconds) }

        val result = runBlocking { localValkeyPersitence.getFnr(fnrCodeResult.code) }
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }
}
