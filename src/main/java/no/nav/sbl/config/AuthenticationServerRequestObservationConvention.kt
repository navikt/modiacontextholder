package no.nav.sbl.config

import io.micrometer.common.KeyValues
import io.micrometer.observation.Observation
import no.nav.sbl.service.AuthContextService
import org.slf4j.LoggerFactory
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention
import org.springframework.http.server.observation.ServerRequestObservationContext
import org.springframework.http.server.observation.ServerRequestObservationConvention
import org.springframework.stereotype.Component

// Legger til ekstra informasjon om autentisert bruker i metrikker basert fra request context
@Component
class AuthenticationServerRequestObservationConvention(
    private val authContextService: AuthContextService,
    private val defaultConvention: ServerRequestObservationConvention = DefaultServerRequestObservationConvention(),
) : ServerRequestObservationConvention by defaultConvention {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun getLowCardinalityKeyValues(context: ServerRequestObservationContext): KeyValues {
        val defaultTags = defaultConvention.getLowCardinalityKeyValues(context)

        // azp_name from token claim, e.g. "dev-gcp:aura:nais-testapp"
        val authorizedPartyName: String = authContextService.getAuthorizedPartyName()
            .orElse("unknown")

        // TODO: Fjern etter testing
        logger.info("Authentication type: $authorizedPartyName")

        return defaultTags.and(KeyValues.of("authorized_party", authorizedPartyName))
    }

    override fun supportsContext(context: Observation.Context): Boolean {
        return context is ServerRequestObservationContext
    }

    override fun getName(): String {
        return defaultConvention.name
    }
}