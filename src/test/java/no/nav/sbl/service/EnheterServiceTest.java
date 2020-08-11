package no.nav.sbl.service;

import no.nav.sbl.consumers.axsys.domain.tilgang.Enhet;
import no.nav.sbl.rest.domain.DecoratorDomain;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import no.nav.sbl.consumers.axsys.AxsysClient;
import no.nav.sbl.consumers.axsys.domain.AxsysTilgangResponse;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnheterServiceTest {

    @Mock
    EnheterCache enhetCache;
    @Mock
    private AxsysClient client;

    @InjectMocks
    EnheterService service;



    @Test
    public void filterer_ut_inaktive_enheter() throws Exception {
        gitt_tilgang_til_enheter(asList(
                new Enhet("0001", emptySet(), "0001"),
                new Enhet("0002", emptySet(), "0002"),
                new Enhet("0003", emptySet(), "0003")
        ));
        gitt_aktive_enheter(asList(
                new DecoratorDomain.Enhet("0002", "0002")
        ));

        List<DecoratorDomain.Enhet> enheter = service.hentEnheter("").get();

        assertThat(enheter).hasSize(1);
        assertThat(enheter.get(0).enhetId).isEqualTo("0002");
    }

    private void gitt_tilgang_til_enheter(List<Enhet> enheter) throws Exception {
        AxsysTilgangResponse resp = new AxsysTilgangResponse();
        resp.enheter = enheter;

        when(client.hentTilgang(any())).thenReturn(resp);
    }

    private void gitt_aktive_enheter(List<DecoratorDomain.Enhet> data) {
        Map<String, DecoratorDomain.Enhet> cache = data
                .stream()
                .collect(Collectors.toMap((e) -> e.enhetId, Function.identity()));

        when(enhetCache.get()).thenReturn(cache);
    }
}
