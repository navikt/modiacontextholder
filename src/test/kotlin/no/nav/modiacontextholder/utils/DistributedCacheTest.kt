package no.nav.modiacontextholder.service

import io.lettuce.core.RedisClient
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import no.nav.modiacontextholder.rest.TestApplication
import no.nav.modiacontextholder.rest.model.DecoratorDomain
import no.nav.modiacontextholder.utils.CacheFactory
import org.koin.core.qualifier.named
import org.koin.test.inject
import org.koin.test.mock.declareMock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DistributedCacheTest : TestApplication() {
    val mockService: EnheterService = mockk()
    val mockEnheter = listOf(DecoratorDomain.Enhet("0000", "Test Enhet", "LOKAL"))

    @Test
    fun `skal cache i redis med riktig key`() =
        testApp {
            val cache = getCache(serializer<String>())
            val res =
                cache.get("testkey") {
                    "testvalue"
                }
            assertEquals("testvalue", res)
            assertEquals("testvalue", Json.decodeFromString<String>(getKey("${cache.cacheName}:testkey")?: ""))
        }

    @Test
    fun `skal bruke cache om den er satt`() =
        testApp {
            coEvery { mockService.hentEnheter("ident", "token") } returns (Result.success(mockEnheter))
            val cache = getCache(serializer<List<DecoratorDomain.Enhet>>())
            cache
                .get("ident") {
                    runBlocking {
                        mockService.hentEnheter(it, "token").getOrDefault(emptyList())
                    }
                }.apply {
                    assertEquals(mockEnheter, this)
                    coVerify { mockService.hentEnheter("ident", "token") }
                }

            cache
                .get("ident") {
                    runBlocking {
                        mockService.hentEnheter(it, "token").getOrDefault(emptyList())
                    }
                }.apply {
                    assertEquals(mockEnheter, this)
                    coVerify(exactly = 1) { mockService.hentEnheter("ident", "token") }
                }
        }

    @Test
    fun `skal bruke cache i ekte scenario`() =
        testApp {
            val mockAd = declareMock<AzureADService>()

            coEvery { mockAd.fetchRoller(any(), any()) } returns
                listOf(
                    AnsattRolle("0000-GA-ENHET_0001", "0001"),
                    AnsattRolle("0000-GA-ENHET_0002", "0002"),
                    AnsattRolle("0000-GA-ENHET_0003", "0003"),
                    AnsattRolle("0000-GA-ENHET_0004", "0004"),
                )
            delKey("enheter:Z999999")

            client.getAuth("/api/v2/decorator").apply {
                coVerify(exactly = 2) { mockAd.fetchRoller(any(), any()) }
                assertNotNull(getKey("enheter:Z999999"))
            }

            client.getAuth("/api/v2/decorator").apply {
                coVerify(exactly = 3) { mockAd.fetchRoller(any(), any()) }
            }
        }

    private fun <V> getCache(serializer: KSerializer<V>) = CacheFactory.createDistributedCache<String, V>(serializer = serializer)

    private fun getKey(key: String): String? {
        val redis: RedisClient by inject(qualifier = named("cache"))

        val connection = redis.connect().sync()
        return connection.get(key)
    }

    private fun delKey(key: String) {
        val redis: RedisClient by inject(qualifier = named("cache"))

        val connection = redis.connect().sync()
        connection.del(key)
    }
}
