package no.nav.sbl.service;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.sbl.rest.domain.DecoratorDomain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import no.nav.sbl.consumers.axsys.AxsysClient;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class EnheterService {
    @Autowired
    private AxsysClient client;

    @Autowired
    private EnheterCache enheterCache;

    @Cacheable("enheterCache")
    public Try<List<DecoratorDomain.Enhet>> hentEnheter(String ident) {
        Map<String, DecoratorDomain.Enhet> aktiveEnheter = enheterCache.get();
        return Try.of(() ->
                client.hentTilgang(ident)
                        .enheter
                        .stream()
                        .map((enhet) -> aktiveEnheter.get(enhet.getEnhetId()))
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparing(DecoratorDomain.Enhet::getEnhetId))
                        .collect(Collectors.toList())
        )
                .onFailure((exception) -> log.error("Kunne ikke hente enheter for {} fra AXSYS", ident, exception));
    }

    public List<DecoratorDomain.Enhet> hentAlleEnheter() {
        return enheterCache.getAll();
    }
}
