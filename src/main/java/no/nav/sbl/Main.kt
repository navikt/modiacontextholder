package no.nav.sbl

import no.nav.common.utils.EnvironmentUtils
import no.nav.common.utils.NaisUtils
import no.nav.common.utils.SslUtils
import no.nav.sbl.config.ApplicationCluster
import no.nav.sbl.config.DatabaseConfig.Companion.MODIACONTEXTHOLDERDB_PASSWORD
import no.nav.sbl.config.DatabaseConfig.Companion.MODIACONTEXTHOLDERDB_URL_PROPERTY
import no.nav.sbl.config.DatabaseConfig.Companion.MODIACONTEXTHOLDERDB_USERNAME
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

            val dbCredentials = NaisUtils.getCredentials("modiacontextholderDB")
            EnvironmentUtils.setProperty(
                MODIACONTEXTHOLDERDB_USERNAME,
                dbCredentials.username,
                EnvironmentUtils.Type.PUBLIC,
            )
            EnvironmentUtils.setProperty(
                MODIACONTEXTHOLDERDB_PASSWORD,
                dbCredentials.password,
                EnvironmentUtils.Type.SECRET,
            )

            val dbUrl = NaisUtils.getFileContent("/var/run/secrets/nais.io/db_config/jdbc_url")
            EnvironmentUtils.setProperty(MODIACONTEXTHOLDERDB_URL_PROPERTY, dbUrl, EnvironmentUtils.Type.PUBLIC)
        }
    }
}
