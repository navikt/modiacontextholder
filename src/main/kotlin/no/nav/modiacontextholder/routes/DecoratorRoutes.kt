package no.nav.modiacontextholder.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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

    suspend fun getEnheter(
        roles: List<String>,
        ident: String,
        userToken: String,
    ): Result<List<DecoratorDomain.Enhet>> {
        if (roles.contains(ROLLE_MODIA_ADMIN)) {
            return Result.success(enheterService.hentAlleEnheter())
        } else {
            return enheterService.hentEnheter(ident, userToken)
        }
    }

    fun exceptionHandlder(throwable: Throwable) =
        if (throwable is HTTPException) {
            throwable
        } else {
            HTTPException(HttpStatusCode.InternalServerError, "Kunne ikke hente data om enheter")
        }

    suspend fun getDecoratorRessurs(
        ident: String,
        userToken: String,
    ): DecoratorConfig {
        val roles = azureADService.fetchRoller(userToken, NavIdent(ident)).map { it.gruppeNavn }
        return getEnheter(roles, ident, userToken)
            .map { enheter -> DecoratorConfig(veilederService.hentVeilederNavn(ident), enheter) }
            .getOrElse { throw exceptionHandlder(it) }
    }

    route("/decorator") {
        /**
         * Get info for the decorator. Includes the users enheter, name and ident
         *
         * @OpenAPITag decorator
         */
        get("/v2") {
            val ident = call.getIdent()
            val token = call.getIdToken()
            call.respond(getDecoratorRessurs(ident, token))
        }

        /**
         * Get info for the decorator. Includes the users enheter, name and ident
         *
         * Deprecated: use v2 instead
         *
         * @OpenAPITag decorator
         */
        get("") {
            val ident = call.getIdent()
            val token = call.getIdToken()
            call.respond(getDecoratorRessurs(ident, token))
        }

        /**
         * Get fnr from aktorID
         *
         * Deprecated: handle this in the app and remove the need for AktorID
         *
         * @OpenAPITag decorator
         */
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
