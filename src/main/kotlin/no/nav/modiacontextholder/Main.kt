package no.nav.modiacontextholder

import io.ktor.server.netty.Netty
import no.nav.modiacontextholder.config.Configuration
import no.nav.personoversikt.common.ktor.utils.KtorServer

fun main() {
    val configuration = Configuration()

    KtorServer
        .create(Netty, port = 4000) {
            modiacontextholderApp(configuration = configuration)
        }.start(wait = true)
}
