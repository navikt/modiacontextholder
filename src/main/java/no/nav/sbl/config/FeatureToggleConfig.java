package no.nav.sbl.config;

import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.featuretoggle.UnleashClientImpl;
import no.nav.common.utils.EnvironmentUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureToggleConfig {

    private static String UNLEASH_URL = EnvironmentUtils.getRequiredProperty("UNLEASH_API_URL");

    @Bean
    public UnleashClient unleashService() {
        return new UnleashClientImpl(UNLEASH_URL, "modiacontextholder");
    }
}
