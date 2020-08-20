import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.route
import no.nav.sbl.dao.ContextDAO
import no.nav.sbl.dao.toDTO

fun Route.contextRoutes(dao : ContextDAO) {
    authenticate {
        route("/context") {
                get("/aktivbruker"){
                    val result = dao.getAktivBruker()
                }
                get("/aktivenhet"){
                    val result = dao.getAktivEnhet()
                    call.respond(result?.toDTO())
                }

                delete("/nullstill"){
                    val result = dao.delete()
                    call.respond(result)
                }
            }

    }
}