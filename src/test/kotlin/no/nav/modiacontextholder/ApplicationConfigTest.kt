package no.nav.modiacontextholder

import no.nav.common.nais.NaisYamlUtils
import no.nav.common.utils.EnvironmentUtils
import org.junit.Test
import java.net.URLClassLoader

class ApplicationConfigTest {
    @Test
    @Throws(Exception::class)
    fun appconfigKanLastesFraQ0Config() {
        injectAzureProperties()
        NaisYamlUtils.loadFromYaml(".nais/nais.yaml")

        URLClassLoader(arrayOf()).loadClass("no.nav.sbl.config.ApplicationConfig").newInstance()
    }

    private fun injectAzureProperties() {
        EnvironmentUtils.setProperty("AZURE_APP_WELL_KNOWN_URL", "http://dummy.io", EnvironmentUtils.Type.PUBLIC)
        EnvironmentUtils.setProperty("AZURE_APP_CLIENT_ID", "clientid", EnvironmentUtils.Type.PUBLIC)
    }
}
