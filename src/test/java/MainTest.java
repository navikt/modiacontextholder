import no.nav.apiapp.ApiApp;
import no.nav.common.nais.utils.NaisYamlUtils;
import no.nav.sbl.config.ApplicationConfig;
import no.nav.sbl.dialogarena.test.SystemProperties;
import no.nav.testconfig.ApiAppTest;

import java.io.IOException;

public class MainTest {

    private static final String APPLICATION_NAME = "modiacontextholder";
    private static final String TEST_PORT = "8290";

    public static void main(String[] args) throws IOException {
        SystemProperties.setFrom(".vault.properties");
        NaisYamlUtils.loadFromYaml(".nais/nais-q0.yaml");
        ApiAppTest.setupTestContext(ApiAppTest.Config.builder().applicationName(APPLICATION_NAME).build());
        Main.setCors();

        ApiApp.runApp(ApplicationConfig.class, new String[]{TEST_PORT});
    }
}
