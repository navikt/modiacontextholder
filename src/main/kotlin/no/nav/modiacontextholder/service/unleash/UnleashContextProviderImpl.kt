package no.nav.modiacontextholder.service.unleash

import io.getunleash.UnleashContext
import io.getunleash.UnleashContextProvider
import io.ktor.server.application.ApplicationCall
import no.nav.modiacontextholder.utils.getIdent

class UnleashContextProviderImpl(
    private val call: ApplicationCall,
) : UnleashContextProvider {
    override fun getContext(): UnleashContext {
        val ident = call.getIdent()

        return UnleashContext
            .builder()
            .apply {
                appName("modiacontextholder")
                environment(System.getProperty("UNLEASH_ENVIRONMENT"))
                userId(ident)
            }.build()
    }
}
