package no.nav.sbl.config;

import no.nav.batch.aspects.RunOnlyOnMasterAspect;
import no.nav.metrics.aspects.CountAspect;
import no.nav.metrics.aspects.TimerAspect;
import no.nav.sbl.selftest.HealthCheckService;
import no.nav.sbl.selftest.IsAliveServlet;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
@ComponentScan("no.nav.sbl")
@Import({
        DatabaseConfig.class,
        ServiceContext.class,
})
public class ApplicationConfig {
    @Bean
    public TimerAspect timerAspect() {
        return new TimerAspect();
    }

    @Bean
    public CountAspect countAspect() {
        return new CountAspect();
    }

    @Bean
    public RunOnlyOnMasterAspect runOnlyOnMasterAspect() {
        return new RunOnlyOnMasterAspect();
    }

    @Bean
    public HealthCheckService healthCheckService() {
        return new HealthCheckService();
    }

    @Bean
    public IsAliveServlet isAliveServlet() {
        return new IsAliveServlet();
    }
}
