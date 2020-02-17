package no.nav.sbl.rest;

import io.vavr.control.Try;
import no.nav.common.auth.SubjectHandler;
import no.nav.sbl.config.FeatureToggle;
import no.nav.sbl.rest.domain.DecoratorDomain;
import no.nav.sbl.rest.domain.DecoratorDomain.DecoratorConfig;
import no.nav.sbl.service.AxsysService;
import no.nav.sbl.service.EnheterService;
import no.nav.sbl.service.LdapService;
import no.nav.sbl.service.VeilederService;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;

import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/decorator")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class DecoratorRessurs {
    private static final String rolleModiaAdmin = "0000-GA-Modia_Admin";

    @Inject
    LdapService ldapService;
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
        return lagDecoratorConfig(ident, hentEnheter(ident));
    }

    @GET
    @Path("/v2")
    public DecoratorConfig hentSaksbehandlerInfoOgEnheterFraAxsys() {
        String ident = getIdent();
        return lagDecoratorConfig(ident, hentEnheter(ident, true));
    }

    private DecoratorConfig lagDecoratorConfig(String ident, Try<List<DecoratorDomain.Enhet>> tryEnheter) {
        return tryEnheter
                .map((enheter) -> new DecoratorConfig(veilederService.hentVeilederNavn(ident), enheter))
                .getOrElseThrow(DecoratorRessurs::exceptionHandler);
    }

    private Try<List<DecoratorDomain.Enhet>> hentEnheter(String ident) {
        return hentEnheter(ident, false);
    }

    private Try<List<DecoratorDomain.Enhet>> hentEnheter(String ident, boolean forceAxsys) {
        if (ldapService.hentVeilederRoller(ident).contains(rolleModiaAdmin)) {
            return Try.success(enheterService.hentAlleEnheter());
        }
        if (forceAxsys || featureToggle.isAxsysEnabled()) {
            return axsysService.hentEnheter(ident);
        } else {
            return enheterService.hentEnheter(ident);
        }
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
