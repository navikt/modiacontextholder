fun Route.draftRoutes(dao: DraftDAO) {
    authenticate {
        route("/context") {
            get {
                withSubject {subject ->
                    val (exact, context) = call.request.queryParameters.parse()
                    val dto = DraftIdentificatorDTO(subject, context)
                    val result = dao.get(dto.fromDTO(), exact)
                    call.respond(result.toDTO())


                     SubjectHandler.getIdent()
                            .map(contextService::hentVeiledersContext)
                            .orElseThrow(() -> new NotAuthorizedException("Fant ikke saksbehandlers ident"));
                }
                get("/aktivbruker"){

                }
                get("/aktivenhet"){}
                delete("/nullstill"){
                }
            }

            post {
                withSubject { subject ->
                    val dto: SaveDraftDTO = call.receive()
                    val result = dao.save(dto.fromDTO(subject))
                    call.respond(result.toDTO())
                }
            }

            delete {
                withSubject { subject ->
                    val dto = DraftIdentificatorDTO(subject, call.receive())
                    dao.delete(dto.fromDTO())
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}