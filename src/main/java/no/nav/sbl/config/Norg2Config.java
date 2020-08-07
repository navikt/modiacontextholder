package no.nav.sbl.config;

import no.nav.sbl.consumers.norg2.Norg2Client;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.util.EnvironmentUtils;
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
        Pingable.Ping.PingMetadata metadata = new Pingable.Ping.PingMetadata(
                "norg2",
                "Norg2 - via " + url,
                "Ping mot Norg2.",
                false
        );

        return () -> {
            try {
                norg2Client.ping();
                return Pingable.Ping.lyktes(metadata);
            } catch (Exception e) {
                return Pingable.Ping.feilet(metadata, e);
            }
        };
    }
}
