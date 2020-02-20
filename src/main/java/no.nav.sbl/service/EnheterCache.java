package no.nav.sbl.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.sbl.rest.domain.DecoratorDomain;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.OrganisasjonEnhetV2;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.informasjon.WSEnhetsstatus;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.WSHentFullstendigEnhetListeRequest;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.meldinger.WSHentFullstendigEnhetListeResponse;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableMap;

@Slf4j
public class EnheterCache {
    private static final long HVER_TOLVTE_TIME = 12 * 3600 * 1000;
    private Map<String, DecoratorDomain.Enhet> cache = unmodifiableMap(new HashMap<>());

    @Inject
    OrganisasjonEnhetV2 organisasjonEnhetV2;

    @Scheduled(fixedRate = HVER_TOLVTE_TIME)
    private void refreshCache() {
        try {
            WSHentFullstendigEnhetListeRequest request = new WSHentFullstendigEnhetListeRequest();
            request.getInkluderEnhetsstatusListe().add(WSEnhetsstatus.AKTIV);
            request.getInkluderEnhetsstatusListe().add(WSEnhetsstatus.UNDER_AVVIKLING);

            WSHentFullstendigEnhetListeResponse response = organisasjonEnhetV2.hentFullstendigEnhetListe(request);

            cache = unmodifiableMap(response.getEnhetListe()
                    .stream()
                    .map((enhet) -> new DecoratorDomain.Enhet(enhet.getEnhetId(), enhet.getEnhetNavn()))
                    .collect(Collectors.toMap(
                            enhet -> enhet.enhetId,
                            Function.identity()
                    )));
        } catch (Exception e) {
            log.error("Kunne ikke hente ut alle aktive enheter fra NORG", e);
        }
    }

    public Map<String, DecoratorDomain.Enhet> get() {
        return this.cache;
    }

    public List<DecoratorDomain.Enhet> getAll() {
        return new ArrayList<>(this.cache.values());
    }
}
