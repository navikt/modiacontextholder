package no.nav.sbl.rest;

import io.vavr.control.Try;
import no.nav.sbl.rest.domain.DecoratorDomain;
import no.nav.sbl.rest.domain.DecoratorDomain.DecoratorConfig;
import no.nav.sbl.rest.domain.DecoratorDomain.FnrAktorId;
import no.nav.sbl.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;


@RestController
@RequestMapping("/api/decorator")
public class DecoratorRessurs {
    private static final String rolleModiaAdmin = "0000-GA-Modia_Admin";

    @Autowired
    LdapService ldapService;
    @Autowired
    EnheterService enheterService;
    @Autowired
    VeilederService veilederService;
    @Autowired
    PdlService pdlService;
    @Autowired
    AuthContextService authContextUtils;

    @GetMapping
    public DecoratorConfig hentSaksbehandlerInfoOgEnheter() {
        return hentSaksbehandlerInfoOgEnheterFraAxsys();
    }

    @GetMapping("/v2")
    public DecoratorConfig hentSaksbehandlerInfoOgEnheterFraAxsys() {
        String ident = getIdent();
        return lagDecoratorConfig(ident, hentEnheter(ident));
    }

    @GetMapping("/aktor/{fnr}")
    public FnrAktorId hentAktorId(@PathVariable("fnr") String fnr) {
        return pdlService.hentIdent(fnr)
                .map((aktorId) -> new FnrAktorId(fnr, aktorId))
                .getOrElseThrow((exception) -> {
                    if (exception instanceof ResponseStatusException) {
                        throw (ResponseStatusException) exception;
                    } else {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown error", exception);
                    }
                });
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

    private String getIdent() {
        return authContextUtils.getIdent()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Fant ingen subjecthandler"));
    }

    private static ResponseStatusException exceptionHandler(Throwable throwable) {
        if (throwable instanceof ResponseStatusException) {
            return (ResponseStatusException) throwable;
        }
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Kunne ikke hente data", throwable);
    }
}
