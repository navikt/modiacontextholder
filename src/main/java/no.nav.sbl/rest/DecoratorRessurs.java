package no.nav.sbl.rest;

import io.vavr.control.Option;
import no.nav.common.auth.SubjectHandler;
import no.nav.sbl.mappers.DecoratorMapper;
import no.nav.sbl.rest.domain.DecoratorDomain.DecoratorConfig;
import no.nav.sbl.service.DecoratorService;
import no.nav.sbl.service.EnheterCache;
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
    DecoratorService decoratorService;

    @Inject
    EnheterCache enheterCache;

    @GET
    public DecoratorConfig hentSaksbehandlerInfoOgEnheter() {
        return Option.ofOptional(SubjectHandler.getIdent())
                .toTry(() -> new WebApplicationException("Fant ingen subjecthandler", 500))
                .flatMap(decoratorService::hentVeilederInfo)
                .map((response) -> DecoratorMapper.map(response, enheterCache.get()))
                .getOrElseThrow((throwable) -> {
                    if (throwable instanceof WebApplicationException) {
                        return (WebApplicationException) throwable;
                    }
                    return new WebApplicationException("Kunne ikke hente data", throwable, 500);
                });
    }
}