package no.nav.modiacontextholder

import io.ktor.server.netty.Netty
import no.nav.common.utils.EnvironmentUtils
import no.nav.modiacontextholder.config.Configuration
import no.nav.personoversikt.common.ktor.utils.KtorServer
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("modiacontextholder.Application")
val logger = log

fun main() {
    val isDev = EnvironmentUtils.getOptionalProperty("NAIS_APP_NAME").isEmpty
    if (isDev) {
        return runLocal()
    }

    val configuration = Configuration()

    KtorServer
        .create(Netty, port = 4000, configure = {
            connectionGroupSize = 8
            workerGroupSize = 8
            callGroupSize = 16
        }) {
            modiacontextholderApp(configuration = configuration)
        }.start(wait = true)
}

fun runLocal() {
    val configuration =
        Configuration(
            aaRegisteretBaseUrl = "http://aaregisteret.local",
            aaRegisteretPublicUrl = "http://aaregistert-public.local",
            salesforceBaseUrl = "http://salesforce.local",
        )

    KtorServer
        .create(Netty, port = 4000) {
            modiacontextholderApp(configuration = configuration, useMock = true)
        }.start(wait = true)
}
