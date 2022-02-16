package no.nav.sbl.config;

import no.nav.common.client.msgraph.CachedMsGraphClient;
import no.nav.common.client.msgraph.MsGraphClient;
import no.nav.common.client.msgraph.MsGraphHttpClient;
import no.nav.common.sts.NaisSystemUserTokenProvider;
import no.nav.common.sts.SystemUserTokenProvider;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.sbl.db.DatabaseCleanerService;
import no.nav.sbl.db.dao.EventDAO;
import no.nav.sbl.redis.Redis;
import no.nav.sbl.redis.RedisConfig;
import no.nav.sbl.service.PdlService;
import no.nav.sbl.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static no.nav.common.utils.EnvironmentUtils.getRequiredProperty;

@Configuration
@Import({
        FeatureToggleConfig.class,
        AxsysConfig.class,
        Norg2Config.class,
        RedisConfig.class
})
public class ServiceContext {
    public static final String SECURITY_TOKEN_SERVICE_DISCOVERYURL = getRequiredProperty("SECURITY_TOKEN_SERVICE_DISCOVERY_URL");

    @Bean
    public ContextService contextService(EventDAO eventDAO, Redis.Publisher redis) {
        return new ContextService(eventDAO, redis);
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


    @Bean
    public MsGraphClient msGraphClient() {
        return new CachedMsGraphClient(
                new MsGraphHttpClient(EnvironmentUtils.getRequiredProperty("MS_GRAPH_CLIENT_URL"))
        );
    }

    @Bean
    public AuthContextService authContextService(MsGraphClient msGraphClient) {
        return new AuthContextService(msGraphClient);
    }
}
