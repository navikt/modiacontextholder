package no.nav.modiacontextholder.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.vavr.control.Try
import no.nav.common.types.identer.NavIdent
import no.nav.modiacontextholder.log
import no.nav.modiacontextholder.rest.FnrRequest
import no.nav.modiacontextholder.rest.model.DecoratorDomain
import no.nav.modiacontextholder.rest.model.DecoratorDomain.DecoratorConfig
import no.nav.modiacontextholder.rest.model.DecoratorDomain.FnrAktorId
import no.nav.modiacontextholder.service.*
import no.nav.modiacontextholder.utils.HTTPException
import no.nav.modiacontextholder.utils.getIdToken
import no.nav.modiacontextholder.utils.getIdent
import org.koin.ktor.ext.inject

private const val ROLLE_MODIA_ADMIN = "0000-GA-Modia_Admin"

fun Route.decoratorRoutes() {
    decoratorRoutesInternal()
    route("/v2") {
        decoratorRoutesInternal()
    }
}

fun Route.decoratorRoutesInternal() {
    val veilederService: VeilederService by inject()
    val enheterService: EnheterService by inject()
    val azureADService: AzureADService by inject()
    val pdlService: PdlService by inject()

    fun getEnheter(
        roles: List<String>,
        ident: String,
    ): Try<List<DecoratorDomain.Enhet>> {
        if (roles.contains(ROLLE_MODIA_ADMIN)) {
            return Try.success(enheterService.hentAlleEnheter())
        } else {
            return enheterService.hentEnheter(ident)
        }
    }

    fun exceptionHandlder(throwable: Throwable) =
        if (throwable is HTTPException) {
            throwable
        } else {
            HTTPException(HttpStatusCode.InternalServerError, "Kunne ikke hente data om enheter")
        }

    fun getDecoratorRessurs(
        ident: String,
        userToken: String,
    ): DecoratorConfig {
        val roles = azureADService.fetchRoller(userToken, NavIdent(ident)).map { it.gruppeNavn }
        return getEnheter(roles, ident)
            .map { enheter -> DecoratorConfig(veilederService.hentVeilederNavn(ident), enheter) }
            .getOrElseThrow(::exceptionHandlder)
    }

    route("/decorator") {
        get("/v2") {
            val ident = call.getIdent()
            val token = call.getIdToken()
            call.respond(getDecoratorRessurs(ident, token))
        }

        get {
            val ident = call.getIdent()
            val token = call.getIdToken()
            call.respond(getDecoratorRessurs(ident, token))
        }

        post("/aktor/hent-fnr") {
            val fnrRequest: FnrRequest = call.receive()
            call.respond(
                pdlService
                    .hentIdent(fnrRequest.fnr)
                    .map { aktorId -> FnrAktorId(fnrRequest.fnr, aktorId) }
                    .getOrElseThrow { exception ->
                        if (exception is HTTPException) {
                            throw exception
                        } else {
                            log.error("Could not get ident", exception)
                            throw HTTPException(HttpStatusCode.BadRequest, "Unknown error")
                        }
                    },
            )
        }
    }
}