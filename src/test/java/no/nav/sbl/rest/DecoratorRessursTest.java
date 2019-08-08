package no.nav.sbl.rest;

import io.vavr.control.Try;
import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.common.auth.SsoToken;
import no.nav.common.auth.Subject;
import no.nav.common.auth.SubjectHandler;
import no.nav.sbl.rest.domain.DecoratorDomain;
import no.nav.sbl.rest.domain.DecoratorDomain.DecoratorConfig;
import no.nav.sbl.service.DecoratorService;
import no.nav.sbl.service.EnheterCache;
import no.nav.virksomhet.organisering.enhetogressurs.v1.Enhet;
import no.nav.virksomhet.organisering.enhetogressurs.v1.Ressurs;
import no.nav.virksomhet.tjenester.enhet.meldinger.v1.WSHentEnhetListeResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DecoratorRessursTest {

    private static final String IDENT = "Z999999";
    private static final Subject MOCK_SUBJECT = new Subject(IDENT, IdentType.InternBruker, SsoToken.oidcToken("asda", emptyMap()));

    @Mock
    DecoratorService service;
    @Mock
    EnheterCache enheter;

    @InjectMocks
    DecoratorRessurs rest;

    @Test
    public void ingen_saksbehandlerident() {
        assertThatThrownBy(() -> rest.hentSaksbehandlerInfoOgEnheter())
                .isInstanceOf(WebApplicationException.class)
                .hasMessage("Fant ingen subjecthandler");
    }

    @Test
    public void feil_ved_henting_av_enheter() {
        when(service.hentVeilederInfo(IDENT)).thenReturn(Try.failure(new IllegalStateException("Noe gikk feil")));

        assertThatThrownBy(() -> SubjectHandler.withSubject(MOCK_SUBJECT, () -> rest.hentSaksbehandlerInfoOgEnheter()))
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .isInstanceOf(WebApplicationException.class)
                .hasMessage("Kunne ikke hente data");
    }

    @Test
    public void returnerer_saksbehandlernavn() {
        gitt_tilgang_til_enheter(emptyList());

        SubjectHandler.withSubject(MOCK_SUBJECT, () -> {
            DecoratorConfig decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter();

            assertThat(decoratorConfig.ident).isEqualTo(IDENT);
            assertThat(decoratorConfig.fornavn).isEqualTo("Fornavn");
            assertThat(decoratorConfig.etternavn).isEqualTo("Etternavn");
        });
    }

    @Test
    public void ingen_aktive_enheter() {
        gitt_tilgang_til_enheter(asList(
                enhet("0001", "Test 1"),
                enhet("0002", "Test 2"),
                enhet("0003", "Test 3")
        ));
        gitt_aktive_enheter(emptyList());

        SubjectHandler.withSubject(MOCK_SUBJECT, () -> {
            DecoratorConfig decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter();
            assertThat(decoratorConfig.enheter).isEqualTo(emptyList());
        });
    }

    @Test
    public void bare_aktive_enheter() {
        gitt_tilgang_til_enheter(asList(
                enhet("0001", "Test 1"),
                enhet("0002", "Test 2"),
                enhet("0003", "Test 3")
        ));
        gitt_aktive_enheter(singletonList(enhet("0002", "Test 2")));

        SubjectHandler.withSubject(MOCK_SUBJECT, () -> {
            DecoratorConfig decoratorConfig = rest.hentSaksbehandlerInfoOgEnheter();
            assertThat(decoratorConfig.enheter).hasSize(1);
            assertThat(decoratorConfig.enheter.get(0).enhetId).isEqualTo("0002");
        });
    }

    private void gitt_tilgang_til_enheter(List<Enhet> data) {
        when(service.hentVeilederInfo(IDENT)).thenReturn(Try.of(() -> {
            WSHentEnhetListeResponse response = new WSHentEnhetListeResponse();
            Ressurs ressurs = new Ressurs();
            ressurs.setRessursId(IDENT);
            ressurs.setFornavn("Fornavn");
            ressurs.setEtternavn("Etternavn");
            response.setRessurs(ressurs);

            response.getEnhetListe().addAll(data);
            return response;
        }));
    }

    private void gitt_aktive_enheter(List<Enhet> data) {
        Map<String, DecoratorDomain.Enhet> cache = data
                .stream()
                .map((enhet) -> new DecoratorDomain.Enhet(enhet.getEnhetId(), enhet.getNavn()))
                .collect(Collectors.toMap((e) -> e.enhetId, Function.identity()));

        when(enheter.get()).thenReturn(cache);
    }

    private static Enhet enhet(String id, String navn) {
        Enhet enhet = new Enhet();
        enhet.setEnhetId(id);
        enhet.setNavn(navn);
        return enhet;
    }
}