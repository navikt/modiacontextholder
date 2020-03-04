package no.nav.sbl.rest;

import io.vavr.control.Try;
import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.common.auth.SsoToken;
import no.nav.common.auth.Subject;
import no.nav.common.auth.SubjectHandler;
import no.nav.sbl.config.FeatureToggle;
import no.nav.sbl.rest.domain.DecoratorDomain;
import no.nav.sbl.rest.domain.DecoratorDomain.DecoratorConfig;
import no.nav.sbl.service.AxsysService;
import no.nav.sbl.service.EnheterService;
import no.nav.sbl.service.LdapService;
import no.nav.sbl.service.VeilederService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.WebApplicationException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DecoratorRessursTest {
    private static final String IDENT = "Z999999";
    private static final Subject MOCK_SUBJECT = new Subject(IDENT, IdentType.InternBruker, SsoToken.oidcToken("asda", emptyMap()));

    @Mock
    LdapService ldapService;
    @Mock
    EnheterService enheterService;
    @Mock
    VeilederService veilederService;
    @Mock
    AxsysService axsysService;
    @Mock
    FeatureToggle featureToggle;

    @InjectMocks
    DecoratorRessurs rest;

    @Test
    public void ingen_saksbehandlerident() {
        gitt_saksbehandler_i_ad();
        shouldWorkRegardlessOfFeatureToggle(() ->
                assertThatThrownBy(() -> rest.hentSaksbehandlerInfoOgEnheter())
                        .isInstanceOf(WebApplicationException.class)
                        .hasMessage("Fant ingen subjecthandler")
        );
    }

    @Test
    public void feil_ved_henting_av_enheter() {
        gitt_saksbehandler_i_ad();
        when(enheterService.hentEnheter(IDENT)).thenReturn(Try.failure(new IllegalStateException("Noe gikk feil")));
        when(axsysService.hentEnheter(IDENT)).thenReturn(Try.failure(new IllegalStateException("Noe gikk feil")));

        shouldWorkRegardlessOfFeatureToggle(() ->
                assertThatThrownBy(() -> SubjectHandler.withSubject(MOCK_SUBJECT, () -> rest.hentSaksbehandlerInfoOgEnheter()))
                        .hasRootCauseInstanceOf(IllegalStateException.class)
                        .isInstanceOf(WebApplicationException.class)
                        .hasMessage("Kunne ikke hente data")
        );
    }

    @Test
    public void returnerer_saksbehandlernavn() {
        gitt_saksbehandler_i_ad();
        gitt_tilgang_til_enheter(emptyList());

        shouldWorkRegardlessOfFeatureToggle(() ->
            SubjectHandler.withSubject(MOCK_SUBJECT, () -> {
                DecoratorConfig decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter();

                assertThat(decoratorConfig.ident).isEqualTo(IDENT);
                assertThat(decoratorConfig.fornavn).isEqualTo("Fornavn");
                assertThat(decoratorConfig.etternavn).isEqualTo("Etternavn");
            })
        );
    }


    @Test
    public void bare_aktive_enheter() {
        gitt_saksbehandler_i_ad();
        gitt_tilgang_til_enheter(asList(
                enhet("0001", "Test 1"),
                enhet("0002", "Test 2"),
                enhet("0003", "Test 3")
        ));

        shouldWorkRegardlessOfFeatureToggle(() ->
            SubjectHandler.withSubject(MOCK_SUBJECT, () -> {
                DecoratorConfig decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter();
                assertThat(decoratorConfig.enheter).hasSize(3);
            })
        );
    }

    @Test
    public void alle_enheter_om_saksbehandler_har_modia_admin() {
        gitt_modia_admin_rolle();
        gitt_saksbehandler_i_ad();
        gitt_tilgang_til_enheter(asList(
                enhet("0001", "Test 1")
        ));

        shouldWorkRegardlessOfFeatureToggle(() -> {
            SubjectHandler.withSubject(MOCK_SUBJECT, () -> {
                DecoratorConfig decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter();
                assertThat(decoratorConfig.enheter).hasSize(5);
            });
        });
    }

    private void gitt_modia_admin_rolle() {
        when(ldapService.hentVeilederRoller(anyString())).thenReturn(asList("0000-GA-Modia_Admin"));
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
        when(axsysService.hentEnheter(IDENT)).thenReturn(Try.of(() -> data));
    }

    private void gitt_saksbehandler_i_ad() {
        when(veilederService.hentVeilederNavn(anyString())).thenReturn(
                new DecoratorDomain.Saksbehandler(IDENT, "Fornavn", "Etternavn")
        );
    }

    private static DecoratorDomain.Enhet enhet(String id, String navn) {
        return new DecoratorDomain.Enhet(id, navn);
    }

    private void shouldWorkRegardlessOfFeatureToggle(Runnable executable) {
        when(featureToggle.isAxsysEnabled()).thenReturn(false);
        executable.run();
        when(featureToggle.isAxsysEnabled()).thenReturn(true);
        executable.run();
    }
}
