package no.nav.sbl.config;

import no.nav.sbl.db.DatabaseCleanerService;
import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.kafka.KafkaConfig;
import no.nav.sbl.service.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        KafkaConfig.class,
        FeatureToggleConfig.class,
        VirksomhetOrganisasjonRessursEnhetV1Config.class,
        VirksomhetOrganisasjonEnhetV2Config.class,
        AxsysConfig.class
})
public class ServiceContext {

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
}
