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