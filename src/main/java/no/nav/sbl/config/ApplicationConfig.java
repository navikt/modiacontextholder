package no.nav.sbl.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.auth.Constants;
import no.nav.common.auth.oidc.filter.OidcAuthenticationFilter;
import no.nav.common.auth.oidc.filter.OidcAuthenticator;
import no.nav.common.auth.oidc.filter.OidcAuthenticatorConfig;
import no.nav.common.auth.subject.IdentType;
import no.nav.common.log.LogFilter;
import no.nav.common.rest.filter.SetStandardHttpHeadersFilter;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.sbl.db.DatabaseCleanerService;
import no.nav.sbl.rest.CleanupServlet;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import static no.nav.common.utils.EnvironmentUtils.isDevelopment;

@Slf4j
@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
@ComponentScan("no.nav.sbl.rest")
@Import({
        CorsConfig.class,
        CacheConfig.class,
        DatabaseConfig.class,
        ServiceContext.class
})
public class ApplicationConfig {
    public static String SRV_USERNAME_PROPERTY = "SRVMODIACONTEXTHOLDER_USERNAME";
    public static String SRV_PASSWORD_PROPERTY = "SRVMODIACONTEXTHOLDER_PASSWORD";

    private static final String issoClientId = EnvironmentUtils.getRequiredProperty("ISSO_CLIENT_ID");
    private static final String issoDiscoveryUrl = EnvironmentUtils.getRequiredProperty("ISSO_DISCOVERY_URL");
    private static final String issoRefreshUrl = EnvironmentUtils.getRequiredProperty("ISSO_REFRESH_URL");
    private static final String fpsakClientId = EnvironmentUtils.getRequiredProperty("FPSAK_CLIENT_ID");
    private static final String azureADClientId = EnvironmentUtils.getRequiredProperty("LOGINSERVICE_OIDC_CLIENTID");
    private static final String azureADDiscoveryUrl = EnvironmentUtils.getRequiredProperty("LOGINSERVICE_OIDC_DISCOVERYURI");
    private static final String azureADClientIdSupstonad = EnvironmentUtils.getRequiredProperty("SUPSTONAD_CLIENTID");

    @Bean
    public FilterRegistrationBean authenticationFilterRegistration() {
        OidcAuthenticatorConfig isso = new OidcAuthenticatorConfig()
                .withClientId(issoClientId)
                .withDiscoveryUrl(issoDiscoveryUrl)
                .withIdTokenCookieName(Constants.OPEN_AM_ID_TOKEN_COOKIE_NAME)
                .withIdentType(IdentType.InternBruker)
                .withRefreshUrl(issoRefreshUrl)
                .withRefreshTokenCookieName(Constants.REFRESH_TOKEN_COOKIE_NAME);

        OidcAuthenticatorConfig fpsak = new OidcAuthenticatorConfig()
                .withClientId(fpsakClientId)
                .withDiscoveryUrl(issoDiscoveryUrl)
                .withIdTokenCookieName(Constants.OPEN_AM_ID_TOKEN_COOKIE_NAME)
                .withIdentType(IdentType.InternBruker)
                .withRefreshUrl(issoRefreshUrl)
                .withRefreshTokenCookieName(Constants.REFRESH_TOKEN_COOKIE_NAME);

        OidcAuthenticatorConfig azureAdSupStonad = new OidcAuthenticatorConfig()
                .withClientId(azureADClientIdSupstonad)
                .withDiscoveryUrl(azureADDiscoveryUrl)
                .withIdentType(IdentType.InternBruker);

        OidcAuthenticatorConfig azureAd = new OidcAuthenticatorConfig()
                .withClientId(azureADClientId)
                .withDiscoveryUrl(azureADDiscoveryUrl)
                .withIdTokenCookieName(Constants.AZURE_AD_ID_TOKEN_COOKIE_NAME)
                .withIdentType(IdentType.InternBruker);

        FilterRegistrationBean<OidcAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OidcAuthenticationFilter(OidcAuthenticator.fromConfigs(isso, fpsak, azureAd, azureAdSupStonad)));
        registration.setOrder(1);
        registration.addUrlPatterns("/rest/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean logFilterRegistrationBean() {
        FilterRegistrationBean<LogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LogFilter("modiacontextholder", isDevelopment().orElse(false)));
        registration.setOrder(2);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean setStandardHeadersFilterRegistrationBean() {
        FilterRegistrationBean<SetStandardHttpHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SetStandardHttpHeadersFilter());
        registration.setOrder(3);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public ServletRegistrationBean<CleanupServlet> cleanupServletServletRegistrationBean(DatabaseCleanerService databaseCleanerService) {
        CleanupServlet cleanupServlet = new CleanupServlet(databaseCleanerService);
        return new ServletRegistrationBean<>(cleanupServlet, "/internal/cleanup");
    }
}
