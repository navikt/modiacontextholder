package no.nav.sbl.config;

import no.nav.sbl.dialogarena.common.cxf.CXFClient;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.tjeneste.virksomhet.organisasjonenhet.v2.binding.OrganisasjonEnhetV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.metrics.MetricsFactory.createTimerProxyForWebService;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Configuration
public class VirksomhetOrganisasjonEnhetV2Config {
    public static final String NORG2_ORGANISASJONENHET_V2_URL = "VIRKSOMHET_ORGANISASJONENHET_V2_ENDPOINTURL";

    @Bean
    public OrganisasjonEnhetV2 organisasjonEnhetV2() {
        return createTimerProxyForWebService("OrganisasjonEnhetV2", new CXFClient<>(OrganisasjonEnhetV2.class)
                .address(resolveOrganisasjonEnhetUrl())
                .configureStsForSystemUser()
                .build(), OrganisasjonEnhetV2.class);
    }

    @Bean
    public Pingable organisasjonEnhetV2Ping() {
        String organisasjonEnhetUrl = resolveOrganisasjonEnhetUrl();
        OrganisasjonEnhetV2 organisasjonEnhetV2 = new CXFClient<>(OrganisasjonEnhetV2.class)
                .address(organisasjonEnhetUrl)
                .configureStsForSystemUser()
                .build();

        PingMetadata metadata = new Pingable.Ping.PingMetadata(
                "organisasjonEnhet_v2",
                "NORG2 - OrganisasjonEnhetV2 via " + organisasjonEnhetUrl,
                "Ping mot OrganisasjonenhetV2 (Norg2).",
                true
        );

        return () -> {
            try {
                organisasjonEnhetV2.ping();
                return Pingable.Ping.lyktes(metadata);
            } catch (Exception e) {
                return Pingable.Ping.feilet(metadata, e);
            }
        };
    }

    private String resolveOrganisasjonEnhetUrl() {
        return getRequiredProperty(NORG2_ORGANISASJONENHET_V2_URL);
    }
}