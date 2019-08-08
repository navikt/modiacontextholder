package no.nav.sbl.mappers;

import no.nav.sbl.rest.domain.DecoratorDomain;
import no.nav.virksomhet.organisering.enhetogressurs.v1.Ressurs;
import no.nav.virksomhet.tjenester.enhet.meldinger.v1.WSHentEnhetListeResponse;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class DecoratorMapper {
    public static DecoratorDomain.DecoratorConfig map(WSHentEnhetListeResponse response, Map<String, DecoratorDomain.Enhet> cache) {
        Ressurs ressurs = response.getRessurs();
        return new DecoratorDomain.DecoratorConfig(
                ressurs.getRessursId(),
                ressurs.getFornavn(),
                ressurs.getEtternavn(),
                enheter(response, cache)
        );
    }

    private static List<DecoratorDomain.Enhet> enheter(WSHentEnhetListeResponse response, Map<String, DecoratorDomain.Enhet> cache) {
        return response
                .getEnhetListe()
                .stream()
                .map((enhet) -> cache.get(enhet.getEnhetId()))
                .filter(Objects::nonNull)
                .collect(toList());
    }
}
