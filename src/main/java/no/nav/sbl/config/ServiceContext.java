package no.nav.sbl.config;

import no.nav.common.sts.NaisSystemUserTokenProvider;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.sbl.db.DatabaseCleanerService;
import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.service.PdlService;
import no.nav.sbl.service.*;
import no.nav.sbl.kafka.KafkaConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static no.nav.common.utils.EnvironmentUtils.getRequiredProperty;

@Configuration
@Import({
        KafkaConfig.class,
        FeatureToggleConfig.class,
        AxsysConfig.class,
        Norg2Config.class
})
public class ServiceContext {
    public static final String SECURITY_TOKEN_SERVICE_DISCOVERYURL = getRequiredProperty("SECURITY_TOKEN_SERVICE_DISCOVERY_URL");

    @Bean
    public ContextService contextService(EventDAO eventDAO, KafkaProducer<String, String> kafka, FeatureToggle featureToggle) {
        return new ContextService(eventDAO, kafka, featureToggle);
    }

    @Bean
    public EventService eventService() {
        return new EventService();
    }

    @Bean
    public DatabaseCleanerService databaseCleanerService() {
        return new DatabaseCleanerService();
    }

    @Bean
    public LdapService ldapService() {
        return new LdapService();
    }

    @Bean
    public EnheterCache enheterCache() {
        return new EnheterCache();
    }

    @Bean
    public VeilederService veilederCache() {
        return new VeilederService();
    }

    @Bean
    public EnheterService enhetService() {
        return new EnheterService();
    }

    @Bean
    public SystemUserTokenProvider systemUserTokenProvider() {
        return new NaisSystemUserTokenProvider(
                SECURITY_TOKEN_SERVICE_DISCOVERYURL,
                getRequiredProperty(ApplicationConfig.SRV_USERNAME_PROPERTY),
                getRequiredProperty(ApplicationConfig.SRV_PASSWORD_PROPERTY)
        );
    }

    @Bean
    public PdlService pdlService(SystemUserTokenProvider sts) {
        return new PdlService(sts);
    }
}