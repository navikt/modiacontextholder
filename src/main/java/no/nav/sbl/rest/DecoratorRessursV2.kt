package no.nav.sbl.rest

import io.vavr.control.Try
import no.nav.common.types.identer.NavIdent
import no.nav.sbl.azure.AzureADService
import no.nav.sbl.rest.domain.DecoratorDomain
import no.nav.sbl.rest.domain.DecoratorDomain.DecoratorConfig
import no.nav.sbl.rest.domain.DecoratorDomain.FnrAktorId
import no.nav.sbl.service.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import kotlin.jvm.optionals.getOrElse

@RestController
@RequestMapping("/api/v2/decorator")
class DecoratorRessursV2(
    private val azureADService: AzureADService,
    private val enheterService: EnheterService,
    private val veilederService: VeilederService,
    private val pdlService: PdlService,
    private val authContextUtils: AuthContextService
) {
    companion object {
        private const val ROLLE_MODIA_ADMIN = "0000-GA-Modia_Admin"
        private fun exceptionHandler(throwable: Throwable): ResponseStatusException {
            return if (throwable is ResponseStatusException) {
                throwable
            } else {
                ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Kunne ikke hente data", throwable)
            }
        }
    }

    @GetMapping
    fun hentSaksbehandlerInfoOgEnheter(): DecoratorConfig {
        return hentSaksbehandlerInfoOgEnheterFraAxsys()
    }

    @GetMapping("/v2")
    fun hentSaksbehandlerInfoOgEnheterFraAxsys(): DecoratorConfig {
        val ident = getIdent()
        return lagDecoratorConfig(ident, hentEnheter(ident))
    }

    @PostMapping("/aktor/hent-fnr")
    fun hentAktorId(@RequestBody fnr: String): FnrAktorId {
        return pdlService.hentIdent(fnr)
            .map { aktorId -> FnrAktorId(fnr, aktorId) }
            .getOrElseThrow { exception ->
                if (exception is ResponseStatusException) {
                    throw exception
                } else {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown error", exception)
                }
            }
    }

    private fun lagDecoratorConfig(ident: String, tryEnheter: Try<List<DecoratorDomain.Enhet>>): DecoratorConfig {
        return tryEnheter
            .map { enheter -> DecoratorConfig(veilederService.hentVeilederNavn(ident), enheter) }
            .getOrElseThrow(DecoratorRessursV2::exceptionHandler)
    }

    private fun hentEnheter(ident: String): Try<List<DecoratorDomain.Enhet>> {
        val userToken = authContextUtils.requireIdToken()
        val roles = azureADService.fetchRoller(userToken, NavIdent(ident)).map { it.gruppeNavn }
        return if (roles.contains(ROLLE_MODIA_ADMIN)) {
            Try.success(enheterService.hentAlleEnheter())
        } else {
            enheterService.hentEnheter(ident)
        }
    }

    private fun getIdent(): String {
        return authContextUtils.getIdent().getOrElse {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fant ingen subjecthandler")
        }
    }
}
