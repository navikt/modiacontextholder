package no.nav.modiacontextholder.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.modiacontextholder.naudit.Audit
import no.nav.modiacontextholder.naudit.Audit.Companion.describe
import no.nav.modiacontextholder.naudit.Audit.Companion.withAudit
import no.nav.modiacontextholder.naudit.AuditIdentifier
import no.nav.modiacontextholder.naudit.AuditResources
import no.nav.modiacontextholder.rest.model.RSNyContext
import no.nav.modiacontextholder.service.ContextService
import no.nav.modiacontextholder.utils.HTTPException
import no.nav.modiacontextholder.utils.getIdent
import org.koin.ktor.ext.inject
import java.util.Optional

fun Route.contextRoutes() {
    val contextService: ContextService by inject()

    route("/context") {
        get("/v2/aktivbruker") { call.respond(contextService.hentAktivBrukerV2(call.getIdent())) }

        get("/aktivbruker") { call.respond(contextService.hentAktivBruker(call.getIdent())) }

        get("/aktivenhet") { call.respond(contextService.hentAktivEnhet(call.getIdent())) }

        get("/v2/aktivenhet") { call.respond(contextService.hentAktivEnhetV2(call.getIdent())) }

        delete("/nullstill") {
            val ident = Optional.of(call.getIdent())
            val referrer = call.request.headers[HttpHeaders.Referrer]
            val url = Pair(AuditIdentifier.REFERER, referrer)
            withAudit(describe(ident, Audit.Action.DELETE, AuditResources.NullstillKontekst, url)) {
                ident.ifPresent(contextService::nullstillContext)
            }
            call.response.status(HttpStatusCode.OK)
        }
        delete {
            val ident = Optional.of(call.getIdent())
            val referrer = call.request.headers[HttpHeaders.Referrer]
            val url = Pair(AuditIdentifier.REFERER, referrer)
            withAudit(describe(ident, Audit.Action.DELETE, AuditResources.NullstillKontekst, url)) {
                ident.ifPresent(contextService::nullstillContext)
            }
            call.response.status(HttpStatusCode.OK)
        }
        delete("/aktivbruker") {
            val ident = Optional.of(call.getIdent())
            val referrer = call.request.headers[HttpHeaders.Referrer]
            val url = Pair(AuditIdentifier.REFERER, referrer)
            withAudit(describe(ident, Audit.Action.DELETE, AuditResources.NullstillBrukerIKontekst, url)) {
                ident.ifPresent(contextService::nullstillAktivBruker)
            }
            call.response.status(HttpStatusCode.OK)
        }

        post {
            val rsNyContext = call.receive<RSNyContext>()
            val ident = Optional.of(call.getIdent())

            val referrer = call.request.headers[HttpHeaders.Referrer]
            val type = Pair(AuditIdentifier.TYPE, rsNyContext.eventType.toString())
            val verdi = Pair(AuditIdentifier.VALUE, rsNyContext.verdi)
            val url = Pair(AuditIdentifier.REFERER, referrer)

            if (ident.isEmpty) {
                throw HTTPException(HttpStatusCode.Unauthorized, "Fant ikke saksbehandlers ident")
            }

            call.respond(
                withAudit(describe(ident, Audit.Action.UPDATE, AuditResources.OppdaterKontekst, type, verdi, url)) {
                    val veilederIdent = ident.get()
                    contextService.oppdaterVeiledersContext(rsNyContext, veilederIdent)
                    contextService.hentVeiledersContext(veilederIdent)
                },
            )
        }
    }
}
