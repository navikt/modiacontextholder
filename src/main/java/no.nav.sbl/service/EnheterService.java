package no.nav.sbl.service;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.sbl.rest.domain.DecoratorDomain;
import no.nav.tjeneste.virksomhet.organisasjonressursenhet.v1.OrganisasjonRessursEnhetV1;
import no.nav.tjeneste.virksomhet.organisasjonressursenhet.v1.meldinger.WSHentEnhetListeRequest;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class EnheterService {
    @Inject
    private OrganisasjonRessursEnhetV1 enhetPorttype;

    @Inject
    private EnheterCache enheterCache;

    @Cacheable("enheterCache")
    public Try<List<DecoratorDomain.Enhet>> hentEnheter(String ident) {
        Map<String, DecoratorDomain.Enhet> aktiveEnheter = enheterCache.get();
        return Try.of(() -> {
            WSHentEnhetListeRequest request = new WSHentEnhetListeRequest();
            request.setRessursId(ident);

            return enhetPorttype
                    .hentEnhetListe(request)
                    .getEnhetListe()
                    .stream()
                    .map((enhet) -> aktiveEnheter.get(enhet.getEnhetId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        })
                .onFailure((exception) -> log.error("Kunne ikke hente enheter for {} fra NORG2", ident, exception));
    }
}
