package no.nav.sbl;

import no.nav.common.nais.NaisYamlUtils;
import no.nav.common.utils.EnvironmentUtils;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

public class ApplicationConfigTest {
    @Test
    public void appconfigKanLastesFraQ0Config() throws Exception {
        injectAzureProperties();
        NaisYamlUtils.loadFromYaml(".nais/nais.yaml");
        
        new URLClassLoader(new URL[]{}).loadClass("no.nav.sbl.config.ApplicationConfig").newInstance();
    }

    private void injectAzureProperties() {
        EnvironmentUtils.setProperty("AZURE_APP_WELL_KNOWN_URL", "http://dummy.io", EnvironmentUtils.Type.PUBLIC);
        EnvironmentUtils.setProperty("AZURE_APP_CLIENT_ID", "clientid", EnvironmentUtils.Type.PUBLIC);
    }
}
