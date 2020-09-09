package no.nav.sbl;

import no.nav.common.nais.NaisYamlUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.test.SystemProperties;
import no.nav.common.test.ssl.SSLTestUtils;
import no.nav.common.test.ssl.TrustAllSSLSocketFactory;
import org.springframework.boot.SpringApplication;

import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

public class MainTest {
    static {
        setupRestClient();
        SystemProperties.setFrom(".vault.properties");
        NaisYamlUtils.NaiseratorSpec naisConfig = NaisYamlUtils.getTemplatedConfig(".nais/qa-template.yaml", new HashMap<>() {{
            put("namespace", "q0");
        }});
        NaisYamlUtils.loadFromYaml(naisConfig);
        SSLTestUtils.disableCertificateChecks();
    }

    public static void main(String[] args) throws IOException {
        SpringApplication application = new SpringApplication(Main.class);
        application.setAdditionalProfiles("local");
        application.run(args);
    }

    private static void setupRestClient() {
        RestClient.setBaseClient(RestClient.baseClientBuilder()
                .sslSocketFactory(new TrustAllSSLSocketFactory(), new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                })
                .build());
    }

}
