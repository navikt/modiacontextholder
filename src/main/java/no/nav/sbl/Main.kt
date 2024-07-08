package no.nav.sbl

import no.nav.common.utils.EnvironmentUtils
import no.nav.common.utils.NaisUtils
import no.nav.common.utils.SslUtils
import no.nav.sbl.config.ApplicationCluster
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (ApplicationCluster.isFss()) {
                setupVault()
            }
            SslUtils.setupTruststore()
            SpringApplication.run(Main::class.java, *args)
        }

        private fun setupVault() {
            val serviceUser = NaisUtils.getCredentials("service_user")
            EnvironmentUtils.setProperty(
                "SRVMODIACONTEXTHOLDER_USERNAME",
                serviceUser.username,
                EnvironmentUtils.Type.PUBLIC,
            )
            EnvironmentUtils.setProperty(
                "SRVMODIACONTEXTHOLDER_PASSWORD",
                serviceUser.password,
                EnvironmentUtils.Type.SECRET,
            )
        }
    }
}
