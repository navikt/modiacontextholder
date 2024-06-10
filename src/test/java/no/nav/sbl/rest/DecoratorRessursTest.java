package no.nav.sbl.rest;

import io.vavr.control.Try;
import no.nav.common.types.identer.AzureObjectId;
import no.nav.sbl.azure.AnsattRolle;
import no.nav.sbl.azure.AzureADService;
import no.nav.sbl.rest.domain.DecoratorDomain;
import no.nav.sbl.rest.domain.DecoratorDomain.DecoratorConfig;
import no.nav.sbl.service.AuthContextService;
import no.nav.sbl.service.EnheterService;
import no.nav.sbl.service.VeilederService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DecoratorRessursTest {
    private static final String IDENT = "Z999999";
    private static final String  GROUP_NAME = "0000-GA-Modia_Admin";
    private static final AzureObjectId azureObjectId = new AzureObjectId("d2987104-63b2-4110-83ac-20ff6afe24a2");

    @Mock
    AzureADService azureADService;
    @Mock
    EnheterService enheterService;
    @Mock
    VeilederService veilederService;
    @Mock
    AuthContextService authContextService;

    @InjectMocks
    DecoratorRessurs rest;

    @Test
    public void ingen_saksbehandlerident() {
        gitt_saksbehandler_i_ad();
        shouldWorkRegardlessOfFeatureToggle(() ->
                assertThatThrownBy(() -> rest.hentSaksbehandlerInfoOgEnheter())
                        .isInstanceOf(ResponseStatusException.class)
                        .hasMessage("500 INTERNAL_SERVER_ERROR \"Fant ingen subjecthandler\"")
        );
    }

    @Test
    public void feil_ved_henting_av_enheter() {
        gitt_logget_inn();
        gitt_saksbehandler_i_ad();
        when(enheterService.hentEnheter(IDENT)).thenReturn(Try.failure(new IllegalStateException("Noe gikk feil")));

        shouldWorkRegardlessOfFeatureToggle(() ->
                assertThatThrownBy(() -> rest.hentSaksbehandlerInfoOgEnheter())
                        .hasRootCauseInstanceOf(IllegalStateException.class)
                        .isInstanceOf(ResponseStatusException.class)
                        .hasMessageStartingWith("500 INTERNAL_SERVER_ERROR \"Kunne ikke hente data\"")
        );
    }

    @Test
    public void returnerer_saksbehandlernavn() {
        gitt_logget_inn();
        gitt_saksbehandler_i_ad();
        gitt_tilgang_til_enheter(emptyList());

        shouldWorkRegardlessOfFeatureToggle(() -> {
                DecoratorConfig decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter();

                assertThat(decoratorConfig.ident).isEqualTo(IDENT);
                assertThat(decoratorConfig.fornavn).isEqualTo("Fornavn");
                assertThat(decoratorConfig.etternavn).isEqualTo("Etternavn");
        });

        shouldWorkRegardlessOfFeatureToggle(() -> {
                    DecoratorConfig decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter();

                    assertThat(decoratorConfig.ident).isEqualTo(IDENT);
                    assertThat(decoratorConfig.fornavn).isEqualTo("Fornavn");
                    assertThat(decoratorConfig.etternavn).isEqualTo("Etternavn");
        });
    }


    @Test
    public void bare_aktive_enheter() {
        gitt_logget_inn();
        gitt_saksbehandler_i_ad();
        gitt_tilgang_til_enheter(asList(
                enhet("0001", "Test 1"),
                enhet("0002", "Test 2"),
                enhet("0003", "Test 3")
        ));

        shouldWorkRegardlessOfFeatureToggle(() -> {
                DecoratorConfig decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter();
                assertThat(decoratorConfig.enheter).hasSize(3);
        });
    }

    @Test
    public void alle_enheter_om_saksbehandler_har_modia_admin() {
        gitt_logget_inn();
        gitt_modia_admin_rolle();
        gitt_saksbehandler_i_ad();
        gitt_tilgang_til_enheter(List.of(
                enhet("0001", "Test 1")
        ));

        shouldWorkRegardlessOfFeatureToggle(() -> {
                DecoratorConfig decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter();
                assertThat(decoratorConfig.enheter).hasSize(5);
        });
    }

    private void gitt_modia_admin_rolle() {
        when(azureADService.fetchRoller(anyString(), any())).thenReturn(List.of(new AnsattRolle(GROUP_NAME, azureObjectId)));

    }

    private void gitt_tilgang_til_enheter(List<DecoratorDomain.Enhet> data) {
        when(enheterService.hentEnheter(IDENT)).thenReturn(Try.of(() -> data));
        when(enheterService.hentAlleEnheter()).thenReturn(asList(
                enhet("0001", "Test 1"),
                enhet("0002", "Test 2"),
                enhet("0003", "Test 3"),
                enhet("0004", "Test 4"),
                enhet("0005", "Test 5")
        ));
    }

    private void gitt_saksbehandler_i_ad() {
        when(veilederService.hentVeilederNavn(anyString())).thenReturn(
                new DecoratorDomain.Saksbehandler(IDENT, "Fornavn", "Etternavn")
        );
    }

    private void gitt_logget_inn() {
        when(authContextService.requireIdToken()).thenReturn("token");
        when(authContextService.getIdent()).thenReturn(Optional.of(IDENT));
    }

    private static DecoratorDomain.Enhet enhet(String id, String navn) {
        return new DecoratorDomain.Enhet(id, navn);
    }

    private void shouldWorkRegardlessOfFeatureToggle(Runnable executable) {
        executable.run();
    }
}
