package no.nav.sbl.rest

import io.vavr.control.Try
import no.nav.common.types.identer.NavIdent
import no.nav.sbl.azure.AnsattRolle
import no.nav.sbl.azure.AzureADService
import no.nav.sbl.rest.domain.DecoratorDomain
import no.nav.sbl.rest.domain.DecoratorDomain.DecoratorConfig
import no.nav.sbl.rest.domain.DecoratorDomain.FnrAktorId
import no.nav.sbl.service.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/decorator")
class DecoratorRessurs(
    val azureADService: AzureADService,
    val enheterService: EnheterService,
    val veilederService: VeilederService,
    val pdlService: PdlService,
    val authContextUtils: AuthContextService
) {
    companion object {
        private const val ROLLE_MODIA_ADMIN = "0000-GA-Modia_Admin"
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

    @GetMapping("/aktor/{fnr}")
    @Deprecated("forRemoval = true")
    fun hentAktorId(@PathVariable("fnr") fnr: String): FnrAktorId {
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
            .getOrElseThrow(::exceptionHandler)
    }

    private fun hentEnheter(ident: String): Try<List<DecoratorDomain.Enhet>> {
        val userToken = authContextUtils.requireIdToken()
        val roles = azureADService.fetchRoller(userToken, NavIdent.of(ident))
            .map(AnsattRolle::gruppeNavn)
        return if (roles.contains(ROLLE_MODIA_ADMIN)) {
            Try.success(enheterService.hentAlleEnheter())
        } else enheterService.hentEnheter(ident)
    }

    private fun getIdent(): String {
        return authContextUtils.ident
            .orElseThrow { ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fant ingen subjecthandler") }
    }

    private fun exceptionHandler(throwable: Throwable): ResponseStatusException {
        return if (throwable is ResponseStatusException) {
            throwable
        } else ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Kunne ikke hente data", throwable)
    }
}