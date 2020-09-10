package no.nav.sbl.rest;

import io.micrometer.core.annotation.Timed;
import no.nav.sbl.db.domain.EventType;
import no.nav.sbl.rest.domain.RSContext;
import no.nav.sbl.rest.domain.RSNyContext;
import no.nav.sbl.service.AuthContextService;
import no.nav.sbl.service.ContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/context")
public class ContextRessurs {

    @Autowired
    private ContextService contextService;
    @Autowired
    AuthContextService authContextUtils;

    @GetMapping
    @Timed
    public RSContext hentVeiledersContext() {
        return authContextUtils.getIdent()
                .map(contextService::hentVeiledersContext)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Fant ikke saksbehandlers ident"));
    }

    @GetMapping("/aktivbruker")
    @Timed("hentAktivBruker")
    public RSContext hentAktivBruker() {
        return authContextUtils.getIdent()
                .map(contextService::hentAktivBruker)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Fant ikke saksbehandlers ident"));
    }

    @GetMapping("/aktivenhet")
    @Timed("hentAktivEnhet")
    public RSContext hentAktivEnhet() {
        return authContextUtils.getIdent()
                .map(contextService::hentAktivEnhet)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Fant ikke saksbehandlers ident"));
    }

    @DeleteMapping
    @Timed("nullstillContext")
    public void nullstillBrukerContext() {
        authContextUtils.getIdent()
                .ifPresent(contextService::nullstillContext);
    }

    @DeleteMapping("/nullstill")
    @Deprecated
    //migrer over til den som ligger på "/" da dette er mest riktig REST-semantisk.
    public void deprecatedNullstillContext() {
        nullstillBrukerContext();
    }

    @DeleteMapping("/aktivbruker")
    @Timed("nullstillAktivBrukerContext")
    public void nullstillAktivBrukerContext() {
        authContextUtils.getIdent().ifPresent(contextService::nullstillAktivBruker);
    }

    @PostMapping
    @Timed("oppdaterVeiledersContext")
    public void oppdaterVeiledersContext(@RequestBody RSNyContext rsNyContext) {
        authContextUtils.getIdent()
                .ifPresent((ident) -> {
                    RSNyContext context = new RSNyContext()
                            .verdi(rsNyContext.verdi)
                            .eventType(EventType.valueOf(rsNyContext.eventType).name());
                    contextService.oppdaterVeiledersContext(context, ident);
                });
    }
}
