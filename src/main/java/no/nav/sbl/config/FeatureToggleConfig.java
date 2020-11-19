package no.nav.sbl.config;

import no.nav.common.featuretoggle.UnleashService;
import no.nav.common.featuretoggle.UnleashServiceConfig;
import no.nav.common.utils.EnvironmentUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureToggleConfig {

    private static String UNLEASH_URL = EnvironmentUtils.getRequiredProperty("UNLEASH_API_URL");;

    @Bean
    public UnleashService unleashService() {
        UnleashServiceConfig config = UnleashServiceConfig.builder()
                .applicationName("modiacontextholder")
                .unleashApiUrl(UNLEASH_URL)
                .build();

        return new UnleashService(config);
    }

    @Bean
    public FeatureToggle feature() {
        return new FeatureToggle(unleashService());
    }
}
