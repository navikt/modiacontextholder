package no.nav.sbl.rest;

import no.nav.common.auth.SubjectHandler;
import no.nav.sbl.config.FeatureToggle;
import no.nav.sbl.rest.domain.DecoratorDomain.DecoratorConfig;
import no.nav.sbl.service.AxsysService;
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
    AxsysService axsysService;
    @Inject
    VeilederService veilederService;
    @Inject
    FeatureToggle featureToggle;

    @GET
    public DecoratorConfig hentSaksbehandlerInfoOgEnheter() {
        String ident = getIdent();

        if (featureToggle.isAxsysEnabled()) {
            return hentSaksbehandlerInfoOgEnheterFraAxsys();
        }

        return enheterService.hentEnheter(ident)
                .map((enheter) -> new DecoratorConfig(veilederService.hentVeilederNavn(ident), enheter))
                .getOrElseThrow(DecoratorRessurs::exceptionHandler);
    }

    @GET
    @Path("/v2")
    public DecoratorConfig hentSaksbehandlerInfoOgEnheterFraAxsys() {
        String ident = getIdent();
        return axsysService.hentEnheter(ident)
                .map((enheter) -> new DecoratorConfig(veilederService.hentVeilederNavn(ident), enheter))
                .getOrElseThrow(DecoratorRessurs::exceptionHandler);
    }

    private static String getIdent() {
        return SubjectHandler.getIdent()
                .orElseThrow(() -> new WebApplicationException("Fant ingen subjecthandler", 500));
    }

    private static WebApplicationException exceptionHandler(Throwable throwable) {
        if (throwable instanceof WebApplicationException) {
            return (WebApplicationException) throwable;
        }
        return new WebApplicationException("Kunne ikke hente data", throwable, 500);
    }
}
