package no.nav.sbl.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.auth.Constants;
import no.nav.common.auth.context.UserRole;
import no.nav.common.auth.oidc.filter.OidcAuthenticationFilter;
import no.nav.common.auth.oidc.filter.OidcAuthenticator;
import no.nav.common.auth.oidc.filter.OidcAuthenticatorConfig;
import no.nav.common.log.LogFilter;
import no.nav.common.rest.filter.SetStandardHttpHeadersFilter;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.sbl.db.DatabaseCleanerService;
import no.nav.sbl.rest.CleanupServlet;
import no.nav.sbl.service.AuthContextService;
import no.nav.sbl.util.AccesstokenServletFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.common.utils.EnvironmentUtils.isDevelopment;

@Slf4j
@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
@Import({
        CorsConfig.class,
        CacheConfig.class,
        DatabaseConfig.class,
        ServiceContext.class
})
public class ApplicationConfig {
    public static String SRV_USERNAME_PROPERTY = "SRVMODIACONTEXTHOLDER_USERNAME";
    public static String SRV_PASSWORD_PROPERTY = "SRVMODIACONTEXTHOLDER_PASSWORD";

    private static final String azureADDiscoveryUrl = EnvironmentUtils.getRequiredProperty("LOGINSERVICE_OIDC_DISCOVERYURI");
    private static final String azureADV2DiscoveryUrl = EnvironmentUtils.getRequiredProperty("AAD_V2_DISCOVERURI");
    private static final String azureADClientId = EnvironmentUtils.getRequiredProperty("LOGINSERVICE_OIDC_CLIENTID");
    private static final String syfoFinnfastlegeClientId = EnvironmentUtils.getRequiredProperty("SYFO_FINNFASTLEGE_CLIENTID");
    private static final String syfoSyfomodiapersonClientId = EnvironmentUtils.getRequiredProperty("SYFO_SYFOMODIAPERSON_CLIENTID");
    private static final String syfoSyfomoteoversiktClientId = EnvironmentUtils.getRequiredProperty("SYFO_SYFOMOTEOVERSIKT_CLIENTID");
    private static final String syfoSyfooversiktClientId = EnvironmentUtils.getRequiredProperty("SYFO_SYFOOVERSIKT_CLIENTID");
    private static final String sosialhjelpModiaClientId = EnvironmentUtils.getRequiredProperty("SOSIALHJELP_MODIA_CLIENTID");
    /**
     * Azure verdiene er automatisk injected til poden siden vi har lagt til azure-konfig i nais-yaml
     */
    private static final String azureOBODiscoveryUrl = EnvironmentUtils.getRequiredProperty("AZURE_APP_WELL_KNOWN_URL");
    private static final String azureOBOClientId = EnvironmentUtils.getRequiredProperty("AZURE_APP_CLIENT_ID");

    @Bean
    public FilterRegistrationBean corsFilterRegistration() {
        CorsFilter corsFilter = new CorsFilter(CorsConfig.allowAllCorsConfig());

        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(corsFilter);
        registration.setOrder(0);
        registration.addUrlPatterns("/api/*");
        registration.addUrlPatterns("/redirect/*");

        return registration;
    }

    @Bean
    public FilterRegistrationBean authenticationFilterRegistration() {
        OidcAuthenticatorConfig azureAd = new OidcAuthenticatorConfig()
                .withClientId(azureADClientId)
                .withDiscoveryUrl(azureADDiscoveryUrl)
                .withIdTokenCookieName(Constants.AZURE_AD_ID_TOKEN_COOKIE_NAME)
                .withUserRole(UserRole.INTERN);

        OidcAuthenticatorConfig azureAdV2 = new OidcAuthenticatorConfig()
                .withClientIds(asList(syfoFinnfastlegeClientId, syfoSyfomodiapersonClientId, syfoSyfomoteoversiktClientId, syfoSyfooversiktClientId, sosialhjelpModiaClientId))
                .withDiscoveryUrl(azureADV2DiscoveryUrl)
                .withIdTokenCookieName(Constants.AZURE_AD_ID_TOKEN_COOKIE_NAME)
                .withUserRole(UserRole.INTERN);

        OidcAuthenticatorConfig azureAdOBO = new OidcAuthenticatorConfig()
                .withClientId(azureOBOClientId)
                .withDiscoveryUrl(azureOBODiscoveryUrl)
                .withUserRole(UserRole.INTERN);

        FilterRegistrationBean<OidcAuthenticationFilter> registration = new FilterRegistrationBean<>();
        List<OidcAuthenticator> authenticators = OidcAuthenticator.fromConfigs(
                azureAd,
                azureAdV2,
                azureAdOBO
        );
        registration.setFilter(new OidcAuthenticationFilter(authenticators));
        registration.setOrder(1);
        registration.addUrlPatterns("/api/*");
        registration.addUrlPatterns("/redirect/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean accesstokenFilterRegistrationBean() {
        FilterRegistrationBean<AccesstokenServletFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AccesstokenServletFilter());
        registration.setOrder(2);
        registration.addUrlPatterns("/api/*");
        registration.addUrlPatterns("/redirect/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean logFilterRegistrationBean() {
        FilterRegistrationBean<LogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LogFilter("modiacontextholder", isDevelopment().orElse(false)));
        registration.setOrder(3);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean setStandardHeadersFilterRegistrationBean() {
        FilterRegistrationBean<SetStandardHttpHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SetStandardHttpHeadersFilter());
        registration.setOrder(4);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public ServletRegistrationBean<CleanupServlet> cleanupServletServletRegistrationBean(DatabaseCleanerService databaseCleanerService, AuthContextService authContextService) {
        CleanupServlet cleanupServlet = new CleanupServlet(databaseCleanerService, authContextService);
        return new ServletRegistrationBean<>(cleanupServlet, "/internal/cleanup");
    }
}
