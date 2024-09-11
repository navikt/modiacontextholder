package no.nav.modiacontextholder

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.vavr.control.Try
import no.nav.common.types.identer.NavIdent
import no.nav.modiacontextholder.rest.model.DecoratorDomain
import no.nav.modiacontextholder.rest.model.DecoratorDomain.DecoratorConfig
import no.nav.modiacontextholder.service.EnheterService
import no.nav.modiacontextholder.service.VeilederService
import org.koin.ktor.ext.inject
import kotlin.jvm.optionals.getOrElse

fun Route.contextRoutes() {
    val veilederService: VeilederService by inject()
    val enheterService: EnheterService by inject()

    route("/v2") {
        route("/decorator") {
            get(Regex("(/v2)?")) {
                val ident = getIdent()
                return lagDecoratorConfig(ident, hentEnheter(ident))
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

    fun getIdent(): String =
        authContextUtils.ident.getOrElse {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fant ingen subjecthandler")
        }
}
