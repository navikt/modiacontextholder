package no.nav.sbl.config;

import no.nav.common.health.HealthCheck;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.selftest.SelfTestCheck;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.sbl.consumers.norg2.Norg2Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Norg2Config {
    public static final String NORG2_URL_PROPERTY = "NORG2_API_URL";

    @Bean
    public Norg2Client norg2Client() {
        return new Norg2Client(
                EnvironmentUtils.getRequiredProperty(NORG2_URL_PROPERTY),
                EnvironmentUtils.getRequiredProperty(ApplicationConfig.SRV_USERNAME_PROPERTY)
        );
    }

    @Bean
    public Pingable organisasjonEnhetV2Ping(Norg2Client norg2Client) {
        String url = EnvironmentUtils.getRequiredProperty(AxsysConfig.AXSYS_URL_PROPERTY);
        HealthCheck check = () -> {
            try {
                norg2Client.ping();
                return HealthCheckResult.healthy();
            } catch (Exception e) {
                return HealthCheckResult.unhealthy(e);
            }
        };

        return () -> new SelfTestCheck("Norg2 via " + url, false, check);
    }
}
