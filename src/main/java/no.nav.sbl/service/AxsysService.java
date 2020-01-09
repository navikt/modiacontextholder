package no.nav.sbl.service;

import io.vavr.control.Try;
import no.nav.sbl.axsys.tilgang.Enhet;
import no.nav.sbl.rest.axsys.AxsysClient;
import no.nav.sbl.rest.domain.DecoratorDomain;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class AxsysService {

    @Inject
    private AxsysClient client;

    @Cacheable("enheterCache")
    public Try<List<DecoratorDomain.Enhet>> hentEnheter(String ident) {
        return Try.of(() ->
                client.hentTilgang(ident)
                        .enheter
                        .stream()
                        .sorted(comparing(Enhet::getEnhetId))
                        .map((enhet) -> new DecoratorDomain.Enhet(enhet.getEnhetId(), enhet.getNavn()))
                        .collect(toList())
        );
    }
}
