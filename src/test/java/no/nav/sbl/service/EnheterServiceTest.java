package no.nav.sbl.service;

import no.nav.sbl.rest.domain.DecoratorDomain;
import no.nav.tjeneste.virksomhet.organisasjonressursenhet.v1.OrganisasjonRessursEnhetV1;
import no.nav.tjeneste.virksomhet.organisasjonressursenhet.v1.informasjon.WSEnhet;
import no.nav.tjeneste.virksomhet.organisasjonressursenhet.v1.meldinger.WSHentEnhetListeResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnheterServiceTest {

    @Mock
    EnheterCache enhetCache;
    @Mock
    OrganisasjonRessursEnhetV1 enhetPorttype;

    @InjectMocks
    EnheterService service;



    @Test
    public void filterer_ut_inaktive_enheter() throws Exception {
        gitt_tilgang_til_enheter(asList(
                new WSEnhet().withEnhetId("0001").withNavn("0001"),
                new WSEnhet().withEnhetId("0002").withNavn("0002"),
                new WSEnhet().withEnhetId("0003").withNavn("0003")
        ));
        gitt_aktive_enheter(asList(
                new DecoratorDomain.Enhet("0002", "0002")
        ));

        List<DecoratorDomain.Enhet> enheter = service.hentEnheter("").get();

        assertThat(enheter).hasSize(1);
        assertThat(enheter.get(0).enhetId).isEqualTo("0002");
    }

    private void gitt_tilgang_til_enheter(List<WSEnhet> enheter) throws Exception {
        WSHentEnhetListeResponse resp = new WSHentEnhetListeResponse();
        resp.getEnhetListe()
                .addAll(enheter);

        when(enhetPorttype.hentEnhetListe(any())).thenReturn(resp);
    }

    private void gitt_aktive_enheter(List<DecoratorDomain.Enhet> data) {
        Map<String, DecoratorDomain.Enhet> cache = data
                .stream()
                .collect(Collectors.toMap((e) -> e.enhetId, Function.identity()));

        when(enhetCache.get()).thenReturn(cache);
    }
}