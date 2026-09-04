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

private const val QUERY_PARAM_KUN_OPPGAVEBEHANDLENDE_ENHETER = "kunOppgavebehandlendeEnheter"

/**
 * Opt-in-filter: `?kunOppgavebehandlendeEnheter=true` gir kun enheter som kan behandle oppgaver.
 * Parameteret er valgfritt, og default (false) beholder dagens oppførsel.
 */
private fun ApplicationCall.kunOppgavebehandlendeEnheter(): Boolean {
    val raw = request.queryParameters[QUERY_PARAM_KUN_OPPGAVEBEHANDLENDE_ENHETER] ?: return false
    return raw.lowercase().toBooleanStrictOrNull()
        ?: throw HTTPException(
            HttpStatusCode.BadRequest,
            "Ugyldig verdi for query-parameter '$QUERY_PARAM_KUN_OPPGAVEBEHANDLENDE_ENHETER'. Forventet 'true' eller 'false'.",
        )
}

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
        kunOppgavebehandlendeEnheter: Boolean,
    ): Result<List<DecoratorDomain.Enhet>> {
        if (roles.contains(ROLLE_MODIA_ADMIN)) {
            return Result.success(enheterService.hentAlleEnheter(kunOppgavebehandlendeEnheter))
        } else {
            return enheterService.hentEnheter(ident, userToken, kunOppgavebehandlendeEnheter)
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
        kunOppgavebehandlendeEnheter: Boolean,
    ): DecoratorConfig {
        val roles = azureADService.fetchRoller(userToken, NavIdent(ident)).map { it.gruppeNavn }
        return getEnheter(roles, ident, userToken, kunOppgavebehandlendeEnheter)
            .map { enheter -> DecoratorConfig(veilederService.hentVeilederNavn(ident), enheter) }
            .getOrElse { throw exceptionHandlder(it) }
    }

    route("/decorator") {
        /**
         * Get info for the decorator. Includes the users enheter, name and ident
         *
         * Query param `kunOppgavebehandlendeEnheter=true` gir kun enheter som kan behandle oppgaver.
         * Utelates parameteren returneres alle enheter (uendret oppførsel).
         *
         * @OpenAPITag decorator
         */
        get("/v2") {
            val ident = call.getIdent()
            val token = call.getIdToken()
            call.respond(getDecoratorRessurs(ident, token, call.kunOppgavebehandlendeEnheter()))
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
            call.respond(getDecoratorRessurs(ident, token, call.kunOppgavebehandlendeEnheter()))
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
