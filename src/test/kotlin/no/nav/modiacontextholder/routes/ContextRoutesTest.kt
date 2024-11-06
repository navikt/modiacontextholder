package no.nav.modiacontextholder.routes

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import no.nav.modiacontextholder.domain.VeilederContextType
import no.nav.modiacontextholder.rest.CodeRequest
import no.nav.modiacontextholder.rest.CodeResponse
import no.nav.modiacontextholder.rest.FnrRequest
import no.nav.modiacontextholder.rest.TestApplication
import no.nav.modiacontextholder.rest.model.*
import no.nav.modiacontextholder.service.ContextService
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ContextRoutesTest : TestApplication() {
    private val ident = "Z999999"
    private val testFnr = "10108000398"

    @Test
    fun testPostContextFraFnrcodeGenerate() =
        testApp {
            val res =
                it.post("/fnr-code/generate") {
                    setBody(FnrRequest(fnr = testFnr))
                    contentType(ContentType.Application.Json)
                }
            assertEquals(HttpStatusCode.OK, res.status)
            val codeResponse = res.body<CodeResponse>()
            assertEquals(testFnr, codeResponse.fnr)
            assertNotNull(codeResponse.code)

            it
                .post("/fnr-code/retrieve") {
                    setBody(CodeRequest(code = codeResponse.code))
                    contentType(ContentType.Application.Json)
                }.apply {
                    val retrieveResponse = this.body<CodeResponse>()
                    assertEquals(codeResponse.code, retrieveResponse.code)
                    assertEquals(codeResponse.fnr, retrieveResponse.fnr)
                }

            it.postAuth("/api/context", RSNyContext(codeResponse.code, VeilederContextType.NY_AKTIV_BRUKER, VerdiType.FNR_KODE)).apply {
                assertEquals(HttpStatusCode.OK, this.status)
                assertEquals(hentFnrFraKontekst(), testFnr)
            }
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
    fun testGetContext() =
        testApp {
            gittFnrIKontekst()
            gittEnhetIKontekst()

            it.get("/api/context").apply {
                assertEquals(HttpStatusCode.OK, this.status)
                assertEquals(call.body<RSContext>().aktivEnhet, "originalEnhet")
                assertEquals(call.body<RSContext>().aktivBruker, "originaltfnr")
            }
        }

    @Test
    fun testGetContextTrailingSlash() =
        testApp {
            gittFnrIKontekst()

            it.get("/api/context/").apply {
                assertEquals(HttpStatusCode.OK, this.status)
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
