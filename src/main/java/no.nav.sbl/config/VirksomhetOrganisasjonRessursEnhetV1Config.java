package no.nav.sbl.config;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.tjeneste.virksomhet.organisasjonressursenhet.v1.OrganisasjonRessursEnhetV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.metrics.MetricsFactory.createTimerProxyForWebService;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Configuration
public class VirksomhetOrganisasjonRessursEnhetV1Config {
    public static final String VIRKSOMHET_ORGANISASJONRESSURSENHET_V1_ENDPOINTURL = "VIRKSOMHET_ORGANISASJONRESSURSENHET_V1_ENDPOINTURL";

    @Bean
    public OrganisasjonRessursEnhetV1 virksomhetEnhet() {
        return createTimerProxyForWebService("enhet_v1", new CXFClient<>(OrganisasjonRessursEnhetV1.class)
                .address(resolveVirksohetEnhetUrl())
                .configureStsForSubject()
                .build(), OrganisasjonRessursEnhetV1.class);
    }

    @Bean
    public Pingable virksomhetEnhetPing() {
        OrganisasjonRessursEnhetV1 virksomhetEnhet = new CXFClient<>(OrganisasjonRessursEnhetV1.class)
                .address(resolveVirksohetEnhetUrl())
                .configureStsForSystemUser()
                .build();

        PingMetadata metadata = new PingMetadata(
                "enhet_v1",
                "VirksomhetEnhet_v1 via " + resolveVirksohetEnhetUrl(),
                "Ping mot VirksomhetEnhet (NORG).",
                true
        );

        return () -> {
            try {
                virksomhetEnhet.ping();
                return Pingable.Ping.lyktes(metadata);
            } catch (Exception e) {
                return Pingable.Ping.feilet(metadata, e);
            }
        };
    }

    private String resolveVirksohetEnhetUrl() {
        return getRequiredProperty(VIRKSOMHET_ORGANISASJONRESSURSENHET_V1_ENDPOINTURL);
    }
}
