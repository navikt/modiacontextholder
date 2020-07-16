package no.nav.sbl.rest;

import io.vavr.control.Try;
import no.nav.common.auth.SubjectHandler;
import no.nav.sbl.rest.domain.DecoratorDomain;
import no.nav.sbl.rest.domain.DecoratorDomain.DecoratorConfig;
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
    VeilederService veilederService;

    @GET
    public DecoratorConfig hentSaksbehandlerInfoOgEnheter() {
        return hentSaksbehandlerInfoOgEnheterFraAxsys();
    }

    @GET
    @Path("/v2")
    public DecoratorConfig hentSaksbehandlerInfoOgEnheterFraAxsys() {
        String ident = getIdent();
        return lagDecoratorConfig(ident, hentEnheter(ident));
    }

    private DecoratorConfig lagDecoratorConfig(String ident, Try<List<DecoratorDomain.Enhet>> tryEnheter) {
        return tryEnheter
                .map((enheter) -> new DecoratorConfig(veilederService.hentVeilederNavn(ident), enheter))
                .getOrElseThrow(DecoratorRessurs::exceptionHandler);
    }

    private Try<List<DecoratorDomain.Enhet>> hentEnheter(String ident) {
        if (ldapService.hentVeilederRoller(ident).contains(rolleModiaAdmin)) {
            return Try.success(enheterService.hentAlleEnheter());
        }
        return enheterService.hentEnheter(ident);
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
