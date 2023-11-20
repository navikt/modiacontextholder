package no.nav.sbl.config;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.sbl.service.unleash.UnleashService;
import no.nav.sbl.service.unleash.UnleashServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureToggleConfig {

    String api = EnvironmentUtils.getRequiredProperty("UNLEASH_SERVER_API_URL") + "/api";
    String apiToken = EnvironmentUtils.getRequiredProperty("UNLEASH_SERVER_API_TOKEN");

    @Bean
    public UnleashService unleashService() {
        UnleashConfig unleashConfig = UnleashConfig.builder()
                .appName("modiacontextholder")
                .environment(System.getProperty("UNLEASH_ENVIRONMENT"))
                .instanceId(System.getProperty("APP_ENVIRONMENT_NAME", "local"))
                .unleashAPI(api)
                .apiKey(apiToken)
                .build();

        Unleash defaultUnleash = new DefaultUnleash(unleashConfig);

        return new UnleashServiceImpl(defaultUnleash);
    }
}
