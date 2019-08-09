package no.nav.sbl.rest;

import no.nav.common.auth.SubjectHandler;
import no.nav.sbl.rest.domain.DecoratorDomain.DecoratorConfig;
import no.nav.sbl.service.EnheterService;
import no.nav.sbl.service.VeilederService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/decorator")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class DecoratorRessurs {

    @Inject
    EnheterService enheterService;

    @Inject
    VeilederService veilederService;

    @GET
    public DecoratorConfig hentSaksbehandlerInfoOgEnheter() {
        String ident = SubjectHandler.getIdent()
                .orElseThrow(() -> new WebApplicationException("Fant ingen subjecthandler", 500));

        return enheterService.hentEnheter(ident)
                .map((enheter) -> new DecoratorConfig(veilederService.hentVeilederNavn(ident), enheter))
                .getOrElseThrow((throwable) -> {
                    if (throwable instanceof WebApplicationException) {
                        return (WebApplicationException) throwable;
                    }
                    return new WebApplicationException("Kunne ikke hente data", throwable, 500);
                });
    }
}
