package no.nav.sbl.config;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.metrics.aspects.CountAspect;
import no.nav.metrics.aspects.TimerAspect;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
@ComponentScan("no.nav.sbl.rest")
@Import({
        DatabaseConfig.class,
        ServiceContext.class,
})
public class ApplicationConfig implements ApiApplication.NaisApiApplication {

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
        apiAppConfigurator
                .issoLogin();
    }

    @Bean
    public TimerAspect timerAspect() {
        return new TimerAspect();
    }

    @Bean
    public CountAspect countAspect() {
        return new CountAspect();
    }

    @Override
    public boolean brukSTSHelsesjekk() {
        return false;
    }
}
