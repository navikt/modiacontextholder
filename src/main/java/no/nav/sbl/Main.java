package no.nav.sbl;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.utils.Credentials;
import no.nav.common.utils.NaisUtils;
import no.nav.common.utils.SslUtils;
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
        SslUtils.setupTruststore();
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
}
