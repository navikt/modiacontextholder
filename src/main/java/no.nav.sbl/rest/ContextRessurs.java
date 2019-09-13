package no.nav.sbl.rest;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.db.domain.EventType;
import no.nav.sbl.rest.domain.RSContext;
import no.nav.sbl.rest.domain.RSNyContext;
import no.nav.sbl.service.ContextService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.brukerdialog.security.context.SubjectHandler.getSubjectHandler;

@Controller
@Path("/context")
@Consumes(APPLICATION_JSON)
public class ContextRessurs {

    @Inject
    private ContextService contextService;

    @GET
    @Timed
    public RSContext hentVeiledersContext() {
        return contextService.hentVeiledersContext(getSubjectHandler().getUid());
    }

    @GET
    @Path("/aktivbruker")
    @Timed(name = "hentAktivBruker")
    public RSContext hentAktivBruker() {
        return contextService.hentAktivBruker(getSubjectHandler().getUid());
    }

    @GET
    @Path("/aktivenhet")
    @Timed(name = "hentAktivEnhet")
    public RSContext hentAktivEnhet() {
        return contextService.hentAktivEnhet(getSubjectHandler().getUid());
    }

    @DELETE
    @Timed(name = "nullstillContext")
    public void nullstillBrukerContext() {
        contextService.nullstillContext(getSubjectHandler().getUid());
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
        contextService.nullstillAktivBruker(getSubjectHandler().getUid());
    }

    @POST
    @Timed(name = "oppdaterVeiledersContext")
    public void oppdaterVeiledersContext(RSNyContext rsNyContext) {
        RSNyContext context = new RSNyContext()
                .verdi(rsNyContext.verdi)
                .eventType(EventType.valueOf(rsNyContext.eventType).name());
        contextService.oppdaterVeiledersContext(context, getSubjectHandler().getUid());
    }
}
