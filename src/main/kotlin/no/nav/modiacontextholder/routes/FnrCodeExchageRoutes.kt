package no.nav.modiacontextholder.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.modiacontextholder.rest.CodeRequest
import no.nav.modiacontextholder.rest.CodeResponse
import no.nav.modiacontextholder.rest.FnrRequest
import no.nav.modiacontextholder.service.FnrCodeExchangeService
import org.koin.ktor.ext.inject

fun Route.fnrCodeExchageRoutes() {
    val fnrCodeExchangeService: FnrCodeExchangeService by inject()

    route("/fnr-code") {
        post("/generate") {
            val fnrRequest = call.receive<FnrRequest>()

            val result = fnrCodeExchangeService.generateAndStoreTempCodeForFnr(fnrRequest.fnr)
            if (result.result.isFailure) {
                call.respond(HttpStatusCode.BadRequest, "Unknown Error")
            } else {
                call.respond(CodeResponse(fnr = fnrRequest.fnr, code = result.code))
            }
        }

        post("/retrieve") {
            val codeRequest = call.receive<CodeRequest>()

            val result = fnrCodeExchangeService.getFnr(codeRequest.code)
            if (result.isFailure) {
                call.respond(HttpStatusCode.BadRequest, "Unknown Error")
            } else {
                val fnr = result.getOrNull()
                if (fnr.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.NotFound, "Fant ikke fnr")
                } else {
                    call.respond(CodeResponse(fnr = fnr, code = codeRequest.code))
                }
            }
        }
    }
}
