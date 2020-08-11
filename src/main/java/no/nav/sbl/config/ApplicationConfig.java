package no.nav.sbl.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.ServletUtil;
import no.nav.apiapp.config.ApiAppConfigurator;
import no.nav.brukerdialog.security.domain.IdentType;
import no.nav.common.oidc.Constants;
import no.nav.common.oidc.auth.OidcAuthenticatorConfig;
import no.nav.sbl.db.DatabaseCleanerService;
import no.nav.sbl.rest.CleanupServlet;
import no.nav.sbl.util.EnvironmentUtils;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;

@Slf4j
@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
@ComponentScan("no.nav.sbl.rest")
@Import({
        CacheConfig.class,
        DatabaseConfig.class,
        ServiceContext.class
})
public class ApplicationConfig implements ApiApplication {
    public static String SRV_USERNAME_PROPERTY = "SRVMODIACONTEXTHOLDER_USERNAME";
    public static String SRV_PASSWORD_PROPERTY = "SRVMODIACONTEXTHOLDER_PASSWORD";

    private static final String issoClientId = EnvironmentUtils.getRequiredProperty("ISSO_CLIENT_ID");
    private static final String issoDiscoveryUrl = EnvironmentUtils.getRequiredProperty("ISSO_DISCOVERY_URL");
    private static final String issoRefreshUrl = EnvironmentUtils.getRequiredProperty("ISSO_REFRESH_URL");
    private static final String fpsakClientId = EnvironmentUtils.getRequiredProperty("FPSAK_CLIENT_ID");
    private static final String azureADClientId = EnvironmentUtils.getRequiredProperty("LOGINSERVICE_OIDC_CLIENTID");
    private static final String azureADDiscoveryUrl = EnvironmentUtils.getRequiredProperty("LOGINSERVICE_OIDC_DISCOVERYURI");
    private static final String azureADClientIdSupstonad = EnvironmentUtils.getRequiredProperty("SUPSTONAD_CLIENTID");

    @Override
    @SneakyThrows
    public void configure(ApiAppConfigurator apiAppConfigurator) {
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

        apiAppConfigurator
                .addOidcAuthenticator(isso)
                .addOidcAuthenticator(fpsak)
                .addOidcAuthenticator(azureAd)
                .addOidcAuthenticator(azureAdSupStonad);
    }

    @Override
    public void startup(ServletContext servletContext) {
        CleanupServlet cleanupServlet = new CleanupServlet(WebApplicationContextUtils.findWebApplicationContext(servletContext).getBean(DatabaseCleanerService.class));
        ServletUtil.leggTilServlet(servletContext, cleanupServlet, "/internal/cleanup");
    }
}
