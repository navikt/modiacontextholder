import no.nav.brukerdialog.security.Constants;
import no.nav.brukerdialog.tools.SecurityConstants;
import no.nav.dialogarena.config.fasit.DbCredentials;
import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.dialogarena.config.fasit.ServiceUser;
import no.nav.dialogarena.config.fasit.ServiceUserCertificate;
import no.nav.dialogarena.config.fasit.dto.RestService;
import no.nav.testconfig.ApiAppTest;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static no.nav.dialogarena.config.fasit.FasitUtils.Zone.FSS;
import static no.nav.sbl.config.DatabaseConfig.*;
import static no.nav.sbl.kafka.KafkaConfig.SRV_PASSWORD;
import static no.nav.sbl.kafka.KafkaConfig.SRV_USERNAME;
import static no.nav.sbl.util.EnvironmentUtils.Type.PUBLIC;
import static no.nav.sbl.util.EnvironmentUtils.Type.SECRET;
import static no.nav.sbl.util.EnvironmentUtils.setProperty;

public class MainTest {

    private static final String APPLICATION_NAME = "modiacontextholder";
    private static final String TEST_PORT = "8390";

    public static void main(String[] args) throws IOException {

        ApiAppTest.setupTestContext(ApiAppTest.Config.builder().applicationName(APPLICATION_NAME).build());

        //kafka
        ServiceUser serviceUser = FasitUtils.getServiceUser("srvmodiacontextholder", APPLICATION_NAME);
        setProperty(SRV_USERNAME, serviceUser.getUsername(), PUBLIC);
        setProperty(SRV_PASSWORD, serviceUser.getPassword(), SECRET);
        setProperty("KAFKA_BROKERS_URL", "b27apvl00045.preprod.local:8443,b27apvl00046.preprod.local:8443,b27apvl00047.preprod.local:8443", PUBLIC);

        // kafka trenger fungerende truststore
        ServiceUserCertificate navTrustStore = FasitUtils.getServiceUserCertificate("nav_truststore", FasitUtils.getDefaultEnvironmentClass());
        File navTrustStoreFile = File.createTempFile("nav_truststore", ".jks");
        FileUtils.writeByteArrayToFile(navTrustStoreFile,navTrustStore.getKeystore());

        setProperty("javax.net.ssl.trustStore", navTrustStoreFile.getAbsolutePath(), PUBLIC);
        setProperty("javax.net.ssl.trustStorePassword", navTrustStore.getKeystorepassword(), SECRET);

        // isso
        ServiceUser srvmodiacontextholder = FasitUtils.getServiceUser("srvmodiacontextholder", APPLICATION_NAME);
        String issoHost = FasitUtils.getBaseUrl("isso-host");
        String issoJWS = FasitUtils.getBaseUrl("isso-jwks");
        String issoISSUER = FasitUtils.getBaseUrl("isso-issuer");
        String issoIsAlive = FasitUtils.getBaseUrl("isso.isalive", FSS);
        ServiceUser isso_rp_user = FasitUtils.getServiceUser("isso-rp-user", APPLICATION_NAME);
        RestService loginUrl = FasitUtils.getRestService("veilarblogin.redirect-url", FasitUtils.getDefaultEnvironment());
        setProperty(Constants.ISSO_HOST_URL_PROPERTY_NAME, issoHost, PUBLIC);
        setProperty(Constants.ISSO_RP_USER_USERNAME_PROPERTY_NAME, isso_rp_user.getUsername(), PUBLIC);
        setProperty(Constants.ISSO_RP_USER_PASSWORD_PROPERTY_NAME, isso_rp_user.getPassword(), SECRET);
        setProperty(Constants.ISSO_JWKS_URL_PROPERTY_NAME, issoJWS, PUBLIC);
        setProperty(Constants.ISSO_ISSUER_URL_PROPERTY_NAME, issoISSUER, PUBLIC);
        setProperty(Constants.ISSO_ISALIVE_URL_PROPERTY_NAME, issoIsAlive, PUBLIC);
        setProperty(SecurityConstants.SYSTEMUSER_USERNAME, srvmodiacontextholder.getUsername(), PUBLIC);
        setProperty(SecurityConstants.SYSTEMUSER_PASSWORD, srvmodiacontextholder.getPassword(), SECRET);
        setProperty(Constants.OIDC_REDIRECT_URL_PROPERTY_NAME, loginUrl.getUrl(), PUBLIC);

        // db
        DbCredentials dbCredentials = FasitUtils.getDbCredentials(APPLICATION_NAME);
        setProperty(MODIACONTEXTHOLDERDB_URL, dbCredentials.url, PUBLIC);
        setProperty(MODIACONTEXTHOLDERDB_USERNAME, dbCredentials.username, PUBLIC);
        setProperty(MODIACONTEXTHOLDERDB_PASSWORD, dbCredentials.password, SECRET);

        Main.main(TEST_PORT);
    }

}