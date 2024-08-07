package no.nav.sbl.rest

import io.micrometer.core.annotation.Timed
import no.nav.sbl.naudit.Audit.Action
import no.nav.sbl.naudit.Audit.Companion.describe
import no.nav.sbl.naudit.Audit.Companion.withAudit
import no.nav.sbl.naudit.AuditIdentifier
import no.nav.sbl.naudit.AuditResources
import no.nav.sbl.rest.model.RSAktivBruker
import no.nav.sbl.rest.model.RSAktivEnhet
import no.nav.sbl.rest.model.RSContext
import no.nav.sbl.rest.model.RSNyContext
import no.nav.sbl.service.AuthContextService
import no.nav.sbl.service.ContextService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import kotlin.Pair

@RestController
@RequestMapping(value = ["/api/context", "/modiacontextholder/api/context"])
class ContextRessurs(
    private val contextService: ContextService,
    private val authContextUtils: AuthContextService,
) {
    @GetMapping
    @Timed
    fun hentVeiledersContext(): RSContext =
        authContextUtils.ident
            .map(contextService::hentVeiledersContext)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Fant ikke saksbehandlers ident") }

    @GetMapping("/aktivbruker")
    @Timed("hentAktivBruker")
    fun hentAktivBruker(): RSContext =
        authContextUtils.ident
            .map(contextService::hentAktivBruker)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Fant ikke saksbehandlers ident") }

    @GetMapping("/v2/aktivbruker")
    @Timed("hentAktivBrukerV2")
    fun hentAktivBrukerV2(): RSAktivBruker =
        authContextUtils.ident
            .map(contextService::hentAktivBrukerV2)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Fant ikke saksbehandlers ident") }

    @GetMapping("/aktivenhet")
    @Timed("hentAktivEnhet")
    fun hentAktivEnhet(): RSContext =
        authContextUtils.ident
            .map(contextService::hentAktivEnhet)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Fant ikke saksbehandlers ident") }

    @GetMapping("/v2/aktivenhet")
    @Timed("hentAktivEnhetV2")
    fun hentAktivEnhetV2(): RSAktivEnhet =
        authContextUtils.ident
            .map(contextService::hentAktivEnhetV2)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Fant ikke saksbehandlers ident") }

    @DeleteMapping
    @Timed("nullstillContext")
    fun nullstillBrukerContext(
        @RequestHeader(value = "referer", required = false) referer: String?,
    ) {
        val ident = authContextUtils.ident
        val url = Pair(AuditIdentifier.REFERER, referer)
        withAudit(describe(ident, Action.DELETE, AuditResources.NullstillKontekst, url)) {
            ident.ifPresent(contextService::nullstillContext)
        }
    }

    @DeleteMapping("/nullstill")
    @Deprecated("migrer over til den som ligger på '/' da dette er mest riktig REST-semantisk.")
    fun deprecatedNullstillContext(
        @RequestHeader(value = "referer", required = false) referer: String?,
    ) {
        nullstillBrukerContext(referer)
    }

    @DeleteMapping("/aktivbruker")
    @Timed("nullstillAktivBrukerContext")
    fun nullstillAktivBrukerContext(
        @RequestHeader(value = "referer", required = false) referer: String?,
    ) {
        val ident = authContextUtils.ident
        val url = Pair(AuditIdentifier.REFERER, referer)
        withAudit(describe(ident, Action.DELETE, AuditResources.NullstillBrukerIKontekst, url)) {
            ident.ifPresent(contextService::nullstillAktivBruker)
        }
    }

    @PostMapping
    @Timed("oppdaterVeiledersContext")
    fun oppdaterVeiledersContext(
        @RequestHeader(value = "referer", required = false) referer: String?,
        @RequestBody rsNyContext: RSNyContext,
    ): RSContext {
        val ident = authContextUtils.ident
        val type = Pair(AuditIdentifier.TYPE, rsNyContext.eventType)
        val verdi = Pair(AuditIdentifier.VALUE, rsNyContext.verdi)
        val url = Pair(AuditIdentifier.REFERER, referer)

        if (ident.isEmpty) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Fant ikke saksbehandlers ident")
        }

        return withAudit(describe(ident, Action.UPDATE, AuditResources.OppdaterKontekst, type, verdi, url)) {
            val veilederIdent = ident.get()
            contextService.oppdaterVeiledersContext(rsNyContext, veilederIdent)
            contextService.hentVeiledersContext(veilederIdent)
        }
    }
}
