import lombok.extern.slf4j.Slf4j;
import no.nav.apiapp.ApiApp;
import no.nav.common.nais.utils.NaisUtils;
import no.nav.sbl.config.ApplicationConfig;
import no.nav.sbl.util.EnvironmentUtils;

import static no.nav.apiapp.rest.NavCorsFilter.CORS_ALLOWED_HEADERS;
import static no.nav.apiapp.rest.NavCorsFilter.CORS_ALLOWED_ORIGINS;
import static no.nav.sbl.config.ApplicationConfig.SRV_PASSWORD_PROPERTY;
import static no.nav.sbl.config.ApplicationConfig.SRV_USERNAME_PROPERTY;
import static no.nav.sbl.config.DatabaseConfig.*;
import static no.nav.sbl.service.LdapService.LDAP_PASSWORD;
import static no.nav.sbl.service.LdapService.LDAP_USERNAME;
import static no.nav.sbl.util.EnvironmentUtils.EnviromentClass.P;
import static no.nav.sbl.util.EnvironmentUtils.EnviromentClass.Q;
import static no.nav.sbl.util.EnvironmentUtils.Type.PUBLIC;
import static no.nav.sbl.util.EnvironmentUtils.Type.SECRET;
import static no.nav.sbl.util.EnvironmentUtils.isEnvironmentClass;
import static no.nav.sbl.util.EnvironmentUtils.setProperty;

@Slf4j
public class Main {
    public static void main(String... args) {
        setupVault();
        setCors();
        ApiApp.runApp(ApplicationConfig.class, args);
    }

    private static void setupVault() {
        NaisUtils.Credentials serviceUser = NaisUtils.getCredentials("service_user");
        EnvironmentUtils.setProperty(SRV_USERNAME_PROPERTY, serviceUser.username, PUBLIC);
        EnvironmentUtils.setProperty(SRV_PASSWORD_PROPERTY, serviceUser.password, SECRET);

        NaisUtils.Credentials srvssolinux = NaisUtils.getCredentials("srvssolinux");
        EnvironmentUtils.setProperty(LDAP_USERNAME, srvssolinux.username, PUBLIC);
        EnvironmentUtils.setProperty(LDAP_PASSWORD, srvssolinux.password, SECRET);

        NaisUtils.Credentials dbCredentials = NaisUtils.getCredentials("modiacontextholderDB");
        EnvironmentUtils.setProperty(MODIACONTEXTHOLDERDB_USERNAME, dbCredentials.username, PUBLIC);
        EnvironmentUtils.setProperty(MODIACONTEXTHOLDERDB_PASSWORD, dbCredentials.password, SECRET);

        String dbUrl = NaisUtils.getFileContent("/var/run/secrets/nais.io/db_config/jdbc_url");
        EnvironmentUtils.setProperty(MODIACONTEXTHOLDERDB_URL_PROPERTY, dbUrl, PUBLIC);
    }

    public static void setCors() {
        setProperty(CORS_ALLOWED_HEADERS, "Accept,Accept-language,Content-Language,Content-Type,Authorization", PUBLIC);
        if (isEnvironmentClass(P)) {
            setProperty(CORS_ALLOWED_ORIGINS, ".adeo.no", PUBLIC);
        } else if (isEnvironmentClass(Q)) {
            String subDomain = ".preprod.local";
            log.info("Setting CORS-headers to {}, YOU SHOULD NOT SEE THIS IN PRODUCTION!!!", subDomain);
            setProperty(CORS_ALLOWED_ORIGINS, subDomain, PUBLIC);
        }
    }
}
