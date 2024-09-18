package no.nav.modiacontextholder.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*
import no.nav.modiacontextholder.utils.getIdent

fun Route.decoratorRoutesV1() {
    route("/decorator") {
        get(Regex("(/v2)?")) {
            val ident = call.getIdent()
        }
    }
}
