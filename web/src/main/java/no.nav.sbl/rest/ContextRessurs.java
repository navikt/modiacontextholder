package no.nav.sbl.rest;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.db.domain.EventType;
import no.nav.sbl.rest.domain.RSContext;
import no.nav.sbl.rest.domain.RSNyContext;
import no.nav.sbl.service.ContextService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.brukerdialog.security.context.SubjectHandler.getSubjectHandler;

@Controller
@Path("/context")
@Consumes(APPLICATION_JSON)
public class ContextRessurs {

    @Inject
    private ContextService contextService;
    @Inject
    private HttpServletRequest request;

    @GET
    @Timed
    public RSContext hentVeiledersContext() {
        return contextService.hentVeiledersContext(getSubjectHandler().getUid(), request.getRemoteAddr());
    }

    @GET
    @Path("/aktivbruker")
    @Timed(name = "hentAktivBruker")
    public RSContext hentAktivBruker() {
        return contextService.hentAktivBruker(getSubjectHandler().getUid(), request.getRemoteAddr());
    }

    @GET
    @Path("/aktivenhet")
    @Timed(name = "hentAktivEnhet")
    public RSContext hentAktivEnhet() {
        return contextService.hentAktivEnhet(getSubjectHandler().getUid());
    }

    @DELETE
    @Path("/nullstill")
    @Timed(name = "nullstillContext")
    public void nullstillBrukerContext() {
        contextService.nullstillContext(getSubjectHandler().getUid());
    }

    @POST
    @Timed(name = "oppdaterVeiledersContext")
    public void oppdaterVeiledersContext(RSNyContext rsNyContext) {
        RSNyContext context = new RSNyContext()
                .withIp(request.getRemoteAddr())
                .withVerdi(rsNyContext.verdi)
                .withEventType(EventType.valueOf(rsNyContext.eventType).name());
        contextService.oppdaterVeiledersContext(context, getSubjectHandler().getUid());
    }
}
