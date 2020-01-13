import no.nav.apiapp.ApiApp;
import no.nav.brukerdialog.security.Constants;
import no.nav.brukerdialog.security.oidc.provider.AzureADB2CConfig;
import no.nav.brukerdialog.tools.SecurityConstants;
import no.nav.common.nais.utils.NaisYamlUtils;
import no.nav.fasit.*;
import no.nav.fasit.dto.RestService;
import no.nav.sbl.config.ApplicationConfig;
import no.nav.sbl.config.AxsysConfig;
import no.nav.sbl.config.VirksomhetOrganisasjonEnhetV2Config;
import no.nav.sbl.config.VirksomhetOrganisasjonRessursEnhetV1Config;
import no.nav.sbl.dialogarena.test.SystemProperties;
import no.nav.testconfig.ApiAppTest;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static no.nav.fasit.FasitUtils.Zone.FSS;
import static no.nav.sbl.config.DatabaseConfig.*;
import static no.nav.sbl.kafka.KafkaConfig.SRV_PASSWORD;
import static no.nav.sbl.kafka.KafkaConfig.SRV_USERNAME;
import static no.nav.sbl.util.EnvironmentUtils.Type.PUBLIC;
import static no.nav.sbl.util.EnvironmentUtils.Type.SECRET;
import static no.nav.sbl.util.EnvironmentUtils.setProperty;

public class MainTest {

    private static final String APPLICATION_NAME = "modiacontextholder";
    private static final String TEST_PORT = "8290";

    public static void main(String[] args) throws IOException {
        SystemProperties.setFrom(".vault.properties");
        NaisYamlUtils.loadFromYaml("nais-q0.yaml");
        ApiAppTest.setupTestContext(ApiAppTest.Config.builder().applicationName(APPLICATION_NAME).build());

        //kafka
//        ServiceUser serviceUser = FasitUtils.getServiceUser("srvmodiacontextholder", "modiacontextholder");
//        setProperty(SRV_USERNAME, serviceUser.getUsername(), PUBLIC);
//        setProperty(SRV_PASSWORD, serviceUser.getPassword(), SECRET);
//        setProperty("KAFKA_BROKERS_URL", "b27apvl00045.preprod.local:8443,b27apvl00046.preprod.local:8443,b27apvl00047.preprod.local:8443", PUBLIC);
//
//        // kafka trenger fungerende truststore
//        ServiceUserCertificate navTrustStore = FasitUtils.getServiceUserCertificate("nav_truststore_pto", FasitUtils.getDefaultEnvironmentClass());
//        File navTrustStoreFile = File.createTempFile("nav_truststore", ".jks");
//        FileUtils.writeByteArrayToFile(navTrustStoreFile, navTrustStore.getKeystore());
//
//        setProperty("javax.net.ssl.trustStore", navTrustStoreFile.getAbsolutePath(), PUBLIC);
//        setProperty("javax.net.ssl.trustStorePassword", navTrustStore.getKeystorepassword(), SECRET);
//
//        LdapConfig ldapConfig = FasitUtils.getLdapConfig();
//        System.setProperty("ldap.basedn", ldapConfig.baseDN);
//        System.setProperty("ldap.url", ldapConfig.url);
//        System.setProperty("ldap.username", ldapConfig.username);
//        System.setProperty("ldap.password", ldapConfig.password);
//
//        // isso
//        String securityTokenService = FasitUtils.getBaseUrl("securityTokenService", FSS);
////        AzureOidcConfigProperties loginserviceOidc = FasitUtils.getAzureOidcConfig("loginservice_oidc", FSS).getProperties();
//        AzureOidcConfigProperties loginserviceOidc = AzureOidcConfigProperties.builder()
//                .clientId("38e07d31-659d-4595-939a-f18dce3446c5")
//                .callbackUri("https://loginservice.nais.preprod.local/callback")
//                .discoveryUri("https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/.well-known/openid-configuration")
//                .build();
//        String issoHost = FasitUtils.getBaseUrl("isso-host");
//        String issoJWS = FasitUtils.getBaseUrl("isso-jwks");
//        String issoISSUER = FasitUtils.getBaseUrl("isso-issuer");
//        String issoIsAlive = FasitUtils.getBaseUrl("isso.isalive", FSS);
//        ServiceUser isso_rp_user = FasitUtils.getServiceUser("isso-rp-user", APPLICATION_NAME);
//        RestService loginUrl = FasitUtils.getRestService("veilarblogin.redirect-url", FasitUtils.getDefaultEnvironment());
//        setProperty(Constants.ISSO_HOST_URL_PROPERTY_NAME, issoHost, PUBLIC);
//        setProperty(Constants.ISSO_RP_USER_USERNAME_PROPERTY_NAME, isso_rp_user.getUsername(), PUBLIC);
//        setProperty(Constants.ISSO_RP_USER_PASSWORD_PROPERTY_NAME, isso_rp_user.getPassword(), SECRET);
//        setProperty(Constants.ISSO_JWKS_URL_PROPERTY_NAME, issoJWS, PUBLIC);
//        setProperty(Constants.ISSO_ISSUER_URL_PROPERTY_NAME, issoISSUER, PUBLIC);
//        setProperty(Constants.ISSO_ISALIVE_URL_PROPERTY_NAME, issoIsAlive, PUBLIC);
//        setProperty(SecurityConstants.STS_URL_KEY, securityTokenService, PUBLIC);
//        setProperty(SecurityConstants.SYSTEMUSER_USERNAME, serviceUser.getUsername(), PUBLIC);
//        setProperty(SecurityConstants.SYSTEMUSER_PASSWORD, serviceUser.getPassword(), SECRET);
//        setProperty(Constants.OIDC_REDIRECT_URL_PROPERTY_NAME, loginUrl.getUrl(), PUBLIC);
//        setProperty(AzureADB2CConfig.INTERNAL_USERS_AZUREAD_B2C_DISCOVERY_URI_PROPERTY_NAME, loginserviceOidc.getDiscoveryUri(), PUBLIC);
//        setProperty(AzureADB2CConfig.INTERNAL_USERS_AZUREAD_B2C_CLIENTID_PROPERTY_NAME, loginserviceOidc.getClientId(), PUBLIC);
//
//        // Tjenester
//        setProperty(VirksomhetOrganisasjonEnhetV2Config.NORG2_ORGANISASJONENHET_V2_URL, FasitUtils.getWebServiceEndpoint("virksomhet:OrganisasjonEnhet_v2").url, PUBLIC);
//        setProperty(VirksomhetOrganisasjonRessursEnhetV1Config.VIRKSOMHET_ORGANISASJONRESSURSENHET_V1_ENDPOINTURL, FasitUtils.getWebServiceEndpoint("virksomhet:OrganisasjonRessursEnhet_v1").url, PUBLIC);
//        setProperty(AxsysConfig.AXSYS_URL_PROPERTY, "https://axsys.nais.preprod.local/api", PUBLIC);
//
//        // db
//        DbCredentials dbCredentials = FasitUtils.getDbCredentials(APPLICATION_NAME);
//        setProperty(MODIACONTEXTHOLDERDB_URL, dbCredentials.url, PUBLIC);
//        setProperty(MODIACONTEXTHOLDERDB_USERNAME, dbCredentials.username, PUBLIC);
//        setProperty(MODIACONTEXTHOLDERDB_PASSWORD, dbCredentials.password, SECRET);

        Main.setCors();
        ApiApp.runApp(ApplicationConfig.class, new String[]{ TEST_PORT });
    }
}
