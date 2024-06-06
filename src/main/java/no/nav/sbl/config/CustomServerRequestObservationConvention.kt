package no.nav.sbl.config

import io.micrometer.common.KeyValues
import io.micrometer.observation.Observation
import no.nav.sbl.service.AuthContextService
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention
import org.springframework.http.server.observation.ServerRequestObservationContext
import org.springframework.stereotype.Component

// Legger til ekstra informasjon om autentisert bruker i metrikker basert fra request context
@Component
class CustomServerRequestObservationConvention(
    private val authContextService: AuthContextService,
) : DefaultServerRequestObservationConvention() {
    override fun getLowCardinalityKeyValues(context: ServerRequestObservationContext): KeyValues {
        val defaultTags = super.getLowCardinalityKeyValues(context)

        // azp_name from token claim, e.g. "dev-gcp:aura:nais-testapp"
        val authorizedPartyName: String = authContextService.getAuthorizedPartyName()
            .orElse("unknown")

        return defaultTags.and(KeyValues.of("authorized_party", authorizedPartyName))
    }

    override fun supportsContext(context: Observation.Context): Boolean {
        return context is ServerRequestObservationContext
    }
}
