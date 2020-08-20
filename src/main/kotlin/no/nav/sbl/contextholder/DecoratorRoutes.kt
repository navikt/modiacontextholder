import io.ktor.auth.authenticate
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.decoratorRoutes() {
    authenticate {
        route("/decorator") {
            get {}

            get("/v2"){
                val ident: String = DecoratorRessurs.getIdent()
                return lagDecoratorConfig(ident, hentEnheter(ident))
            }

            get("/aktor/{fnr}"){}

        }
    }
}