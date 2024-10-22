package no.nav.modiacontextholder.service

import io.lettuce.core.RedisClient
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import no.nav.common.client.axsys.AxsysClient
import no.nav.common.client.axsys.AxsysEnhet
import no.nav.common.types.identer.EnhetId
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
    val mockEnheter = listOf(DecoratorDomain.Enhet("0000", "Test Enhet"))

    @Test
    fun `skal cache i redis med riktig key`() =
        testApp {
            val cache = getCache(serializer<String>())
            val res =
                cache.get("testkey") {
                    "testvalue"
                }
            assertEquals("testvalue", res)
            assertEquals("testvalue", Json.decodeFromString<String>(getKey("${cache.cacheName}:testkey")))
        }

    @Test
    fun `skal bruke cache om den er satt`() =
        testApp {
            coEvery { mockService.hentEnheter("ident") } returns (Result.success(mockEnheter))
            val cache = getCache(serializer<List<DecoratorDomain.Enhet>>())
            cache
                .get("ident") {
                    runBlocking {
                        mockService.hentEnheter(it).getOrDefault(emptyList())
                    }
                }.apply {
                    assertEquals(mockEnheter, this)
                    coVerify { mockService.hentEnheter("ident") }
                }

            cache
                .get("ident") {
                    runBlocking {
                        mockService.hentEnheter(it).getOrDefault(emptyList())
                    }
                }.apply {
                    assertEquals(mockEnheter, this)
                    coVerify(exactly = 1) { mockService.hentEnheter("ident") }
                }
        }

    @Test
    fun `skal bruke cache i ekte scenario`() =
        testApp {
            val mockAxsys = declareMock<AxsysClient>()

            every { mockAxsys.hentTilganger(any()) } returns
                listOf(
                    AxsysEnhet().setEnhetId(EnhetId("0001")),
                    AxsysEnhet().setEnhetId(EnhetId("0002")),
                    AxsysEnhet().setEnhetId(EnhetId("0003")),
                )
            delKey("enheter:Z999999")

            client.getAuth("/api/v2/decorator").apply {
                verify(exactly = 1) { mockAxsys.hentTilganger(any()) }
                assertNotNull(getKey("enheter:Z999999"))
            }

            client.getAuth("/api/v2/decorator").apply {
                verify(exactly = 1) { mockAxsys.hentTilganger(any()) }
            }
        }

    private fun <V> getCache(serializer: KSerializer<V>) = CacheFactory.createDistributedCache<String, V>(serializer = serializer)

    private fun getKey(key: String): String {
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
