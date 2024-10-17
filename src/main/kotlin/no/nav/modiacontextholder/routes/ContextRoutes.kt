package no.nav.modiacontextholder.routes

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
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
import java.util.*

fun Route.contextRoutes() {
    val contextService: ContextService by inject()

    /**
     * Context routes
     */
    route("/context") {
        /**
         * Get the users active context
         *
         * @OpenAPITag context
         */
        get("") { call.respond(contextService.hentVeiledersContext(call.getIdent())) }

        /**
         * Get the users active user from context
         *
         * @OpenAPITag context
         */
        get("/v2/aktivbruker") { call.respond(contextService.hentAktivBrukerV2(call.getIdent())) }

        /**
         * Get the users active user from context
         *
         * Deprecated: use v2 instead
         *
         * @OpenAPITag context
         */
        get("/aktivbruker") { call.respond(contextService.hentAktivBruker(call.getIdent())) }

        /**
         * Get the users active enhet from context
         *
         * Deprecated: use v2 instead
         *
         * @OpenAPITag context
         */
        get("/aktivenhet") { call.respond(contextService.hentAktivEnhet(call.getIdent())) }

        /**
         * Get the users active enhet from context
         *
         * @OpenAPITag context
         */
        get("/v2/aktivenhet") { call.respond(contextService.hentAktivEnhetV2(call.getIdent())) }

        /**
         * Clear the users current context
         *
         * @OpenAPITag context
         */
        delete("") {
            val ident = Optional.of(call.getIdent())
            val referrer = call.request.headers[HttpHeaders.Referrer]
            val url = Pair(AuditIdentifier.REFERER, referrer)
            withAudit(describe(ident, Audit.Action.DELETE, AuditResources.NullstillKontekst, listOf(url))) {
                ident.ifPresent(contextService::nullstillContext)
            }
            call.response.status(HttpStatusCode.OK)
        }
        /**
         * Clear the users active user context
         *
         * @OpenAPITag context
         */
        delete("/aktivbruker") {
            val ident = Optional.of(call.getIdent())
            val referrer = call.request.headers[HttpHeaders.Referrer]
            val url = Pair(AuditIdentifier.REFERER, referrer)
            withAudit(describe(ident, Audit.Action.DELETE, AuditResources.NullstillBrukerIKontekst, listOf(url))) {
                ident.ifPresent(contextService::nullstillAktivBruker)
            }
            call.response.status(HttpStatusCode.OK)
        }

        /**
         * Update context
         *
         * @OpenAPITag context
         */
        post("") {
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
                withAudit(
                    describe(
                        ident,
                        Audit.Action.UPDATE,
                        AuditResources.OppdaterKontekst,
                        listOf(type, verdi, url),
                    ),
                ) {
                    val veilederIdent = ident.get()
                    contextService.oppdaterVeiledersContext(rsNyContext, veilederIdent)
                    contextService.hentVeiledersContext(veilederIdent)
                },
            )
        }
    }
}
