package no.nav.modiacontextholder.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import no.nav.modiacontextholder.config.Configuration
import no.nav.modiacontextholder.domain.VeilederContextType
import no.nav.modiacontextholder.redis.TestUtils
import no.nav.modiacontextholder.redis.TestUtils.WithRedis.Companion.PASSWORD
import no.nav.modiacontextholder.rest.TestApplication
import no.nav.modiacontextholder.rest.model.RSAktivBruker
import no.nav.modiacontextholder.rest.model.RSAktivEnhet
import no.nav.modiacontextholder.rest.model.RSNyContext
import no.nav.modiacontextholder.service.ContextService
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals

class ContextRoutesTest : TestApplication() {
    private val ident = "Z999999"

    companion object {
        val withRedis = TestUtils.WithRedis()

        @JvmStatic
        @BeforeAll
        fun setup() {
            TestUtils.WithRedis.startContainer()
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            TestUtils.WithRedis.stopContainer()
        }
    }

    private val hostAndPort = withRedis.redisHostAndPort()
    private val redisUri = "redis://default:$PASSWORD@$hostAndPort"

    @BeforeEach
    fun beforeEach() {
        this.configuration = Configuration(redisUri = redisUri)
    }

    @Test
    fun testDeleteContext() =
        testApp {
            gittFnrIKontekst()

            it.delete("/api/context").apply {
                assertEquals(HttpStatusCode.OK, this.status)
                assertEquals(hentFnrFraKontekst(), null)
            }
        }

    @Test
    fun testPostContext() =
        testApp {
            it.postAuth("/api/context", RSNyContext("nybruker", VeilederContextType.NY_AKTIV_BRUKER)).apply {
                assertEquals(HttpStatusCode.OK, this.status)
                assertEquals(hentFnrFraKontekst(), "nybruker")
            }
        }

    @Test
    fun testDeleteContextAktivbruker() =
        testApp {
            gittEnhetIKontekst()
            gittFnrIKontekst()

            it.delete("/api/context/aktivbruker").apply {
                assertEquals(HttpStatusCode.OK, this.status)
                assertEquals(hentFnrFraKontekst(), null)
                assertEquals(hentEnhetFraKontekst(), "originalEnhet")
            }
        }

    @Test
    fun testGetContextAktivbruker() =
        testApp {
            gittFnrIKontekst()

            it.get("/api/context/v2/aktivbruker").apply {
                assertEquals(HttpStatusCode.OK, this.status)
                assertEquals(Json.decodeFromString<RSAktivBruker>(this.bodyAsText()).aktivBruker, "originaltfnr")
            }
        }

    @Test
    fun testGetContextAktivenhet() =
        testApp {
            gittEnhetIKontekst()

            it.get("/api/context/v2/aktivenhet").apply {
                assertEquals(HttpStatusCode.OK, this.status)
                assertEquals(Json.decodeFromString<RSAktivEnhet>(this.bodyAsText()).aktivEnhet, "originalEnhet")
            }
        }

    @Test
    fun testDeleteContextNullstill() =
        testApp {
            gittFnrIKontekst()
            gittEnhetIKontekst()

            it.delete("/api/context/nullstill").apply {
                assertEquals(HttpStatusCode.OK, this.status)
                assertEquals(hentFnrFraKontekst(), null)
                assertEquals(hentEnhetFraKontekst(), null)
            }
        }

    private fun gittFnrIKontekst() {
        val contextService: ContextService by inject()
        contextService.oppdaterVeiledersContext(RSNyContext("originaltfnr", VeilederContextType.NY_AKTIV_BRUKER), ident)

        assertEquals(contextService.hentAktivBrukerV2(ident).aktivBruker, "originaltfnr")
    }

    private fun gittEnhetIKontekst() {
        val contextService: ContextService by inject()
        contextService.oppdaterVeiledersContext(RSNyContext("originalEnhet", VeilederContextType.NY_AKTIV_ENHET), ident)

        assertEquals(contextService.hentAktivEnhetV2(ident).aktivEnhet, "originalEnhet")
    }

    private fun hentFnrFraKontekst(): String? {
        val contextService: ContextService by inject()

        return contextService.hentAktivBrukerV2(ident).aktivBruker
    }

    private fun hentEnhetFraKontekst(): String? {
        val contextService: ContextService by inject()

        return contextService.hentAktivEnhetV2(ident).aktivEnhet
    }
}
