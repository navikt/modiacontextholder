package no.nav.sbl.config;

import no.nav.common.featuretoggle.UnleashService;
import no.nav.common.featuretoggle.UnleashServiceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureToggleConfig {

    private static String UNLEASH_API_URL = "https://unleash.nais.adeo.no/api/";

    @Bean
    public UnleashService unleashService() {
        UnleashServiceConfig config = UnleashServiceConfig.builder()
                .applicationName("modiacontextholder")
                .unleashApiUrl(UNLEASH_API_URL)
                .build();

        return new UnleashService(config);
    }

    @Bean
    public FeatureToggle feature() {
        return new FeatureToggle(unleashService());
    }
}
