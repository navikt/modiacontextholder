import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext
import no.nav.sbl.dao.EventDAO
import no.nav.sbl.dao.toDTO

fun Route.eventRoutes(dao: EventDAO) {
    authenticate {
        route("/events") {
            get("{aktorid}"){
                    val aktorid = call.parameters["aktorid"].toString()
                    val result = dao.get(aktorid)
                    call.respond(result.toDTO())
            }
        }
    }
}
