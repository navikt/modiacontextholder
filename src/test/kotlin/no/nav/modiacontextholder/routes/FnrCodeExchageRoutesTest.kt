package no.nav.modiacontextholder.routes

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.modiacontextholder.rest.CodeRequest
import no.nav.modiacontextholder.rest.CodeResponse
import no.nav.modiacontextholder.rest.FnrRequest
import no.nav.modiacontextholder.rest.TestApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FnrCodeExchageRoutesTest : TestApplication() {
    private val testFnr = "10108000398"

    @Test
    fun testPostFnrcodeGenerate() =
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
        }
}
