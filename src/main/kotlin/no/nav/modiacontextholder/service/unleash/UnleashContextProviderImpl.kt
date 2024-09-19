package no.nav.modiacontextholder.service.unleash

import io.getunleash.UnleashContext
import io.getunleash.UnleashContextProvider
import no.nav.common.log.MDCConstants
import org.slf4j.MDC

class UnleashContextProviderImpl : UnleashContextProvider {
    override fun getContext(): UnleashContext {
        val ident = MDC.get(MDCConstants.MDC_USER_ID)

        return UnleashContext
            .builder()
            .apply {
                appName("modiacontextholder")
                environment(System.getProperty("UNLEASH_ENVIRONMENT"))
                userId(ident)
            }.build()
    }
}
