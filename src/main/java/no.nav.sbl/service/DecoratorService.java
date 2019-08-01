package no.nav.sbl.service;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.virksomhet.tjenester.enhet.meldinger.v1.WSHentEnhetListeRequest;
import no.nav.virksomhet.tjenester.enhet.meldinger.v1.WSHentEnhetListeResponse;
import no.nav.virksomhet.tjenester.enhet.v1.Enhet;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;

@Slf4j
public class DecoratorService {
    @Inject
    private Enhet enhetPorttype;

    @Cacheable("decoratorCache")
    public Try<WSHentEnhetListeResponse> hentVeilederInfo(String ident) {
        return Try.of(() -> {
            WSHentEnhetListeRequest request = new WSHentEnhetListeRequest();
            request.setRessursId(ident);

            return enhetPorttype.hentEnhetListe(request);
        })
                .onFailure((exception) -> log.error("Kunne ikke hente enheter for {} fra NORG2", ident, exception));
    }
}
