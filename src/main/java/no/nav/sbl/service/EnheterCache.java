package no.nav.sbl.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.sbl.consumers.norg2.Norg2Client;
import no.nav.sbl.consumers.norg2.domain.Norg2EnheterResponse;
import no.nav.sbl.rest.domain.DecoratorDomain;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

@Slf4j
public class EnheterCache {
    private static final long HVER_TOLVTE_TIME = 12 * 3600 * 1000;
    private Map<String, DecoratorDomain.Enhet> cache = unmodifiableMap(new HashMap<>());
    private List<DecoratorDomain.Enhet> cacheList = unmodifiableList(new ArrayList<>());

    @Inject
    Norg2Client norg2Client;

    @Scheduled(fixedRate = HVER_TOLVTE_TIME)
    private void refreshCache() {
        try {
            Norg2EnheterResponse response = norg2Client.hentAlleEnheter();

            cacheList = unmodifiableList(response.enheter
                    .stream()
                    .map((enhet) -> new DecoratorDomain.Enhet(enhet.getEnhetNr(), enhet.getNavn()))
                    .sorted(Comparator.comparing(DecoratorDomain.Enhet::getEnhetId))
                    .collect(Collectors.toList())
            );

            cache = unmodifiableMap(cacheList
                    .stream()
                    .collect(Collectors.toMap(
                            enhet -> enhet.enhetId,
                            Function.identity()
                    )));

        } catch (Exception e) {
            log.error("Kunne ikke hente ut alle aktive enheter fra NORG2-rest", e);
        }
    }

    public Map<String, DecoratorDomain.Enhet> get() {
        return this.cache;
    }

    public List<DecoratorDomain.Enhet> getAll() {
        return cacheList;
    }
}
