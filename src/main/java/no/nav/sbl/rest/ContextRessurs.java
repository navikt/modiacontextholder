package no.nav.sbl.rest;

import io.micrometer.core.annotation.Timed;
import kotlin.Pair;
import no.nav.sbl.db.domain.EventType;
import no.nav.sbl.naudit.AuditIdentifier;
import no.nav.sbl.naudit.AuditResources;
import no.nav.sbl.rest.domain.RSAktivBruker;
import no.nav.sbl.rest.domain.RSAktivEnhet;
import no.nav.sbl.rest.domain.RSContext;
import no.nav.sbl.rest.domain.RSNyContext;
import no.nav.sbl.service.AuthContextService;
import no.nav.sbl.service.ContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static no.nav.sbl.naudit.Audit.Action.DELETE;
import static no.nav.sbl.naudit.Audit.Action.UPDATE;
import static no.nav.sbl.naudit.Audit.describe;
import static no.nav.sbl.naudit.Audit.withAudit;


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

    @GetMapping("/v2/aktivbruker")
    @Timed("hentAktivBrukerV2")
    public RSAktivBruker hentAktivBrukerV2() {
        return authContextUtils.getIdent().map(contextService::hentAktivBrukerV2)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Fant ikke saksbehandlers ident"));
    }

    @GetMapping("/aktivenhet")
    @Timed("hentAktivEnhet")
    public RSContext hentAktivEnhet() {
        return authContextUtils.getIdent()
                .map(contextService::hentAktivEnhet)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Fant ikke saksbehandlers ident"));
    }

    @GetMapping("/v2/aktivenhet")
    @Timed("hentAktivEnhetV2")
    public RSAktivEnhet hentAktivEnhetV2() {
        return authContextUtils.getIdent()
                .map(contextService::hentAktivEnhetV2)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Fant ikke saksbehandlers ident"));
    }

    @DeleteMapping
    @Timed("nullstillContext")
    public void nullstillBrukerContext(@RequestHeader(value = "referer", required = false) String referer) {
        Optional<String> ident = authContextUtils.getIdent();
        Pair<AuditIdentifier, String> url = new Pair<>(AuditIdentifier.REFERER, referer);
        withAudit(describe(ident, DELETE, AuditResources.NullstillKontekst, url), () -> {
            ident.ifPresent(contextService::nullstillContext);
            return null;
        });
    }

    @DeleteMapping("/nullstill")
    @Deprecated
    //migrer over til den som ligger på "/" da dette er mest riktig REST-semantisk.
    public void deprecatedNullstillContext(@RequestHeader(value = "referer", required = false) String referer) {
        nullstillBrukerContext(referer);
    }

    @DeleteMapping("/aktivbruker")
    @Timed("nullstillAktivBrukerContext")
    public void nullstillAktivBrukerContext(@RequestHeader(value = "referer", required = false) String referer) {
        Optional<String> ident = authContextUtils.getIdent();
        Pair<AuditIdentifier, String> url = new Pair<>(AuditIdentifier.REFERER, referer);
        withAudit(describe(ident, DELETE, AuditResources.NullstillBrukerIKontekst, url), () -> {
            ident.ifPresent(contextService::nullstillAktivBruker);
            return null;
        });
    }

    @PostMapping
    @Timed("oppdaterVeiledersContext")
    public RSContext oppdaterVeiledersContext(@RequestHeader(value = "referer", required = false) String referer, @RequestBody RSNyContext rsNyContext) {
        Optional<String> ident = authContextUtils.getIdent();
        RSNyContext context = new RSNyContext()
                .verdi(rsNyContext.verdi)
                .eventType(EventType.valueOf(rsNyContext.eventType).name());
        Pair<AuditIdentifier, String> type = new Pair<>(AuditIdentifier.TYPE, context.eventType);
        Pair<AuditIdentifier, String> verdi = new Pair<>(AuditIdentifier.VALUE, context.verdi);
        Pair<AuditIdentifier, String> url = new Pair<>(AuditIdentifier.REFERER, referer);

        if (ident.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Fant ikke saksbehandlers ident");
        }


        return withAudit(describe(ident, UPDATE, AuditResources.OppdaterKontekst, type, verdi, url), () -> {
            String veilederIdent = ident.get();
            contextService.oppdaterVeiledersContext(context, veilederIdent);
            return contextService.hentVeiledersContext(veilederIdent);
        });
    }
}
