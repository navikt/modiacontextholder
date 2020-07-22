package no.nav.sbl;

import no.nav.common.nais.utils.NaisYamlUtils;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;

public class ApplicationConfigTest {
    @Test
    public void appconfigKanLastesFraQ0Config() throws Exception {
        NaisYamlUtils.loadFromYaml(".nais/nais.yaml");
        new URLClassLoader(new URL[]{}).loadClass("no.nav.sbl.config.ApplicationConfig").newInstance();
    }
}
