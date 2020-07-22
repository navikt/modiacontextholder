package no.nav.sbl.rest;

import no.nav.common.auth.SubjectHandler;
import no.nav.metrics.aspects.Timed;
import no.nav.sbl.db.domain.EventType;
import no.nav.sbl.rest.domain.RSContext;
import no.nav.sbl.rest.domain.RSNyContext;
import no.nav.sbl.service.ContextService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/context")
@Consumes(APPLICATION_JSON)
public class ContextRessurs {

    @Inject
    private ContextService contextService;

    @GET
    @Timed
    public RSContext hentVeiledersContext() {
        return SubjectHandler.getIdent()
                .map(contextService::hentVeiledersContext)
                .orElseThrow(() -> new NotAuthorizedException("Fant ikke saksbehandlers ident"));
    }

    @GET
    @Path("/aktivbruker")
    @Timed(name = "hentAktivBruker")
    public RSContext hentAktivBruker() {
        return SubjectHandler.getIdent()
                .map(contextService::hentAktivBruker)
                .orElseThrow(() -> new NotAuthorizedException("Fant ikke saksbehandlers ident"));
    }

    @GET
    @Path("/aktivenhet")
    @Timed(name = "hentAktivEnhet")
    public RSContext hentAktivEnhet() {
        return SubjectHandler.getIdent()
                .map(contextService::hentAktivEnhet)
                .orElseThrow(() -> new NotAuthorizedException("Fant ikke saksbehandlers ident"));
    }

    @DELETE
    @Timed(name = "nullstillContext")
    public void nullstillBrukerContext() {
        SubjectHandler.getIdent()
                .ifPresent(contextService::nullstillContext);
    }

    @DELETE
    @Deprecated
    @Path("/nullstill")
    //migrer over til den som ligger på "/" da dette er mest riktig REST-semantisk.
    public void deprecatedNullstillContext() {
        nullstillBrukerContext();
    }

    @DELETE
    @Path("/aktivbruker")
    @Timed(name = "nullstillAktivBrukerContext")
    public void nullstillAktivBrukerContext() {
        SubjectHandler.getIdent().ifPresent(contextService::nullstillAktivBruker);
    }

    @POST
    @Timed(name = "oppdaterVeiledersContext")
    public void oppdaterVeiledersContext(RSNyContext rsNyContext) {
        SubjectHandler.getIdent()
                .ifPresent((ident) -> {
                    RSNyContext context = new RSNyContext()
                            .verdi(rsNyContext.verdi)
                            .eventType(EventType.valueOf(rsNyContext.eventType).name());
                    contextService.oppdaterVeiledersContext(context, ident);
                });
    }
}
