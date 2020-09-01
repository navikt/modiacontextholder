package no.nav.sbl;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.utils.Credentials;
import no.nav.common.utils.NaisUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static no.nav.common.utils.EnvironmentUtils.Type.PUBLIC;
import static no.nav.common.utils.EnvironmentUtils.Type.SECRET;
import static no.nav.common.utils.EnvironmentUtils.setProperty;
import static no.nav.sbl.config.ApplicationConfig.SRV_PASSWORD_PROPERTY;
import static no.nav.sbl.config.ApplicationConfig.SRV_USERNAME_PROPERTY;
import static no.nav.sbl.config.DatabaseConfig.*;
import static no.nav.sbl.service.LdapService.LDAP_PASSWORD;
import static no.nav.sbl.service.LdapService.LDAP_USERNAME;

@Slf4j
@SpringBootApplication
public class Main {
    public static void main(String... args) {
        setupVault();
        setCors();
        SpringApplication.run(Main.class, args);
    }

    private static void setupVault() {
        Credentials serviceUser = NaisUtils.getCredentials("service_user");
        setProperty(SRV_USERNAME_PROPERTY, serviceUser.username, PUBLIC);
        setProperty(SRV_PASSWORD_PROPERTY, serviceUser.password, SECRET);

        Credentials srvssolinux = NaisUtils.getCredentials("srvssolinux");
        setProperty(LDAP_USERNAME, srvssolinux.username, PUBLIC);
        setProperty(LDAP_PASSWORD, srvssolinux.password, SECRET);

        Credentials dbCredentials = NaisUtils.getCredentials("modiacontextholderDB");
        setProperty(MODIACONTEXTHOLDERDB_USERNAME, dbCredentials.username, PUBLIC);
        setProperty(MODIACONTEXTHOLDERDB_PASSWORD, dbCredentials.password, SECRET);

        String dbUrl = NaisUtils.getFileContent("/var/run/secrets/nais.io/db_config/jdbc_url");
        setProperty(MODIACONTEXTHOLDERDB_URL_PROPERTY, dbUrl, PUBLIC);
    }

    public static void setCors() {
//        setProperty(CORS_ALLOWED_HEADERS, "Accept,Accept-language,Content-Language,Content-Type,Authorization", PUBLIC);
//        if (isEnvironmentClass(P)) {
//            setProperty(CORS_ALLOWED_ORIGINS, ".adeo.no", PUBLIC);
//        } else if (isEnvironmentClass(Q)) {
//            String subDomain = ".preprod.local";
//            log.info("Setting CORS-headers to {}, YOU SHOULD NOT SEE THIS IN PRODUCTION!!!", subDomain);
//            setProperty(CORS_ALLOWED_ORIGINS, subDomain, PUBLIC);
//        }
    }
}
