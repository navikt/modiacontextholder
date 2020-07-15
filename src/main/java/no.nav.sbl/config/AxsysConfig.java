package no.nav.sbl.config;

import no.nav.sbl.dialogarena.common.cxf.StsSecurityConstants;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.rest.axsys.AxsysClient;
import no.nav.sbl.service.AxsysService;
import no.nav.sbl.util.EnvironmentUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxsysConfig {
    public static final String AXSYS_URL_PROPERTY = "AXSYS_REST_API_URL";

    @Bean
    public AxsysService axsysService() {
        return new AxsysService();
    }

    @Bean
    public AxsysClient axsysClient() {
        return new AxsysClient(
                EnvironmentUtils.getRequiredProperty(AxsysConfig.AXSYS_URL_PROPERTY),
                EnvironmentUtils.getRequiredProperty(StsSecurityConstants.SYSTEMUSER_USERNAME)
        );
    }

    @Bean
    public Pingable axsysPing(AxsysClient axsysClient) {
        String url = EnvironmentUtils.getRequiredProperty(AxsysConfig.AXSYS_URL_PROPERTY);
        Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(
                "axsys",
                "Axsys - via " + url,
                "Ping mot Axsys.",
                false
        );

        return () -> {
            try {
                axsysClient.ping();
                return Pingable.Ping.lyktes(metadata);
            } catch (Exception e) {
                return Pingable.Ping.feilet(metadata, e);
            }
        };
    }
}
