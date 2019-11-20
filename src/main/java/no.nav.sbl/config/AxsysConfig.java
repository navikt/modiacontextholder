package no.nav.sbl.config;

import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.service.AxsysService;
import no.nav.sbl.util.EnvironmentUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxsysConfig {
    public static final String AXSYS_URL = "AXSYS_REST_API_URL";

    @Bean
    public AxsysService axsysService() {
        return new AxsysService();
    }

    @Bean
    public Pingable axsysPing(AxsysService axsysService) {
        String url = EnvironmentUtils.getRequiredProperty(AxsysConfig.AXSYS_URL);
        Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(
                "axsys",
                "Axsys - via " + url,
                "Ping mot Axsys.",
                false
        );

        return () -> {
            try {
                axsysService.ping();
                return Pingable.Ping.lyktes(metadata);
            } catch (Exception e) {
                return Pingable.Ping.feilet(metadata, e);
            }
        };
    }
}
