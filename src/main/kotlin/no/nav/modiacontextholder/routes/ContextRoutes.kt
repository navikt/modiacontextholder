package no.nav.modiacontextholder.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.modiacontextholder.service.ContextService
import no.nav.modiacontextholder.utils.getIdent
import org.koin.ktor.ext.inject

fun Route.contextRoutes() {
    val contextService: ContextService by inject()

    route("/context") {
        route("/v2/aktivbruker") {
            get {
                call.respond(contextService.hentAktivEnhetV2(call.getIdent()))
            }
        }

        route("/aktivbruker") {}
        get { call.respond(contextService.hentAktivBruker(call.getIdent())) }
    }
}
