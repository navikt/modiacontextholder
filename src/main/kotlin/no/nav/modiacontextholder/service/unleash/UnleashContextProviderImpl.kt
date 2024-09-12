package no.nav.modiacontextholder.service.unleash

import io.getunleash.UnleashContext
import io.getunleash.UnleashContextProvider
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class UnleashContextProviderImpl(
    private val authContextService: AuthContextService,
) : UnleashContextProvider {
    override fun getContext(): UnleashContext {
        val ident = authContextService.ident.orElse(null)

        val remoteAddr =
            runCatching {
                val attributes = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
                attributes.getRequest().remoteAddr
            }.getOrNull()

        return UnleashContext
            .builder()
            .apply {
                appName("modiacontextholder")
                environment(System.getProperty("UNLEASH_ENVIRONMENT"))
                userId(ident)
                if (remoteAddr != null) {
                    remoteAddress(remoteAddr)
                }
            }.build()
    }
}
