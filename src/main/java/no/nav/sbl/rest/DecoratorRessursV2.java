package no.nav.sbl.rest;

import io.vavr.control.Try;
import no.nav.common.types.identer.NavIdent;
import no.nav.sbl.azure.AnsattRolle;
import no.nav.sbl.azure.AzureADService;
import no.nav.sbl.rest.domain.DecoratorDomain;
import no.nav.sbl.rest.domain.DecoratorDomain.DecoratorConfig;
import no.nav.sbl.rest.domain.DecoratorDomain.FnrAktorId;
import no.nav.sbl.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v2/decorator")
public class DecoratorRessursV2 {
    private static final String rolleModiaAdmin = "0000-GA-Modia_Admin";

    @Autowired
    AzureADService azureADService;
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

    @PostMapping("/aktor/hent-fnr")
    public FnrAktorId hentAktorId(@RequestBody String fnr) {
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
                .getOrElseThrow(DecoratorRessursV2::exceptionHandler);
    }

    private Try<List<DecoratorDomain.Enhet>> hentEnheter(String ident) {
        String userToken = authContextUtils.requireIdToken();
        List<String> roles = azureADService.fetchRoller(userToken, new NavIdent(ident)).stream().map(AnsattRolle::getGruppeNavn).collect(Collectors.toList());
        if (roles.contains(rolleModiaAdmin)) {
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
