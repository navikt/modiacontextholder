package no.nav.sbl;

import no.nav.common.utils.Credentials;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.common.utils.NaisUtils;
import no.nav.common.utils.SslUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

import static no.nav.common.utils.EnvironmentUtils.Type.PUBLIC;
import static no.nav.common.utils.EnvironmentUtils.Type.SECRET;
import static no.nav.common.utils.EnvironmentUtils.setProperty;
import static no.nav.sbl.config.ApplicationConfig.SRV_PASSWORD_PROPERTY;
import static no.nav.sbl.config.ApplicationConfig.SRV_USERNAME_PROPERTY;
import static no.nav.sbl.config.DatabaseConfig.*;

@SpringBootApplication
public class Main {
    public static void main(String... args) {
        setupVault();
        SslUtils.setupTruststore();
        SpringApplication.run(Main.class, args);
    }

    private static void setupVault() {
        String clusterName = String.valueOf(EnvironmentUtils.getClusterName());
        if(!Objects.equals(clusterName, "dev-gcp") && !Objects.equals(clusterName, "prod-gcp")) {
            Credentials serviceUser = NaisUtils.getCredentials("service_user");
            setProperty(SRV_USERNAME_PROPERTY, serviceUser.username, PUBLIC);
            setProperty(SRV_PASSWORD_PROPERTY, serviceUser.password, SECRET);

            Credentials dbCredentials = NaisUtils.getCredentials("modiacontextholderDB");
            setProperty(MODIACONTEXTHOLDERDB_USERNAME, dbCredentials.username, PUBLIC);
            setProperty(MODIACONTEXTHOLDERDB_PASSWORD, dbCredentials.password, SECRET);

            String dbUrl = NaisUtils.getFileContent("/var/run/secrets/nais.io/db_config/jdbc_url");
            setProperty(MODIACONTEXTHOLDERDB_URL_PROPERTY, dbUrl, PUBLIC);
        }
    }
}
