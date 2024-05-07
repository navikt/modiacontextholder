package no.nav.sbl.config;

import no.nav.common.health.HealthCheck;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.selftest.SelfTestCheck;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.sbl.consumers.axsys.AxsysClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxsysConfig {
    public static final String AXSYS_URL_PROPERTY = "AXSYS_URL";

    @Bean
    public AxsysClient axsysClient() {
        return new AxsysClient(
                EnvironmentUtils.getRequiredProperty(AxsysConfig.AXSYS_URL_PROPERTY),
                EnvironmentUtils.getRequiredProperty(ApplicationConfig.SRV_USERNAME_PROPERTY)
        );
    }

    @Bean
    public Pingable axsysPing(AxsysClient client) {
        String url = EnvironmentUtils.getRequiredProperty(AxsysConfig.AXSYS_URL_PROPERTY);
        HealthCheck check = () -> {
            try {
                client.ping();
                return HealthCheckResult.healthy();
            } catch (Exception e) {
                return HealthCheckResult.unhealthy(e);
            }
        };
        return () -> new SelfTestCheck(
                "Axsys - via " + url,
                false,
                check
        );
    }
}
