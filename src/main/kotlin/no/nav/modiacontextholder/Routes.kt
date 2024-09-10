package no.nav.modiacontextholder

import io.ktor.server.application.*
import io.ktor.server.routing.*
import no.nav.common.types.identer.NavIdent
import no.nav.sbl.rest.DecoratorRessursV2
import no.nav.sbl.rest.DecoratorRessursV2.Companion.ROLLE_MODIA_ADMIN
import no.nav.sbl.rest.model.DecoratorDomain
import no.nav.sbl.rest.model.DecoratorDomain.DecoratorConfig
import kotlin.jvm.optionals.getOrElse

fun Route.contextRoutes() {
    route("/v2") {
        route("/decorator") {
            get(Regex("(/v2)?")) {
            }

            post("/aktor/hent-fnr") {
            }
        }
    }

    fun lagDecoratorConfig(
        ident: String,
        tryEnheter: Try<List<DecoratorDomain.Enhet>>,
    ): DecoratorConfig =
        tryEnheter
            .map { enheter -> DecoratorConfig(veilederService.hentVeilederNavn(ident), enheter) }
            .getOrElseThrow(DecoratorRessursV2::exceptionHandler)

    fun hentEnheter(ident: String): Try<List<DecoratorDomain.Enhet>> {
        val userToken = authContextUtils.requireIdToken()
        val roles = azureADService.fetchRoller(userToken, NavIdent(ident)).map { it.gruppeNavn }
        return if (roles.contains(ROLLE_MODIA_ADMIN)) {
            Try.success(enheterService.hentAlleEnheter())
        } else {
            enheterService.hentEnheter(ident)
        }
    }

    private fun getIdent(): String =
        authContextUtils.ident.getOrElse {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fant ingen subjecthandler")
        }
}
