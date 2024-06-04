package no.nav.sbl.config

import io.micrometer.common.KeyValues
import io.micrometer.observation.Observation
import org.slf4j.LoggerFactory
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention
import org.springframework.http.server.observation.ServerRequestObservationContext
import org.springframework.http.server.observation.ServerRequestObservationConvention
import org.springframework.stereotype.Component

// Legger til ekstra informasjon om autentisert bruker i metrikker basert fra request context
@Component
class AuthenticationServerRequestObservationConvention(
    private val defaultConvention: ServerRequestObservationConvention = DefaultServerRequestObservationConvention(),
) : ServerRequestObservationConvention by defaultConvention {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun getLowCardinalityKeyValues(context: ServerRequestObservationContext): KeyValues {
        val defaultTags = defaultConvention.getLowCardinalityKeyValues(context)

        // TODO: Legg til mer informasjon om bruker, men pass på å kun logge klientidentifikator for maskin-til-maskin-tokens
        val authenticationType = context.carrier.userPrincipal?.javaClass?.simpleName
            ?: "unknown"
        // TODO: Fjern
        logger.info("Authentication type: $authenticationType")

        val authenticationTypeLabel = KeyValues.of("authentication_type", authenticationType)

        return defaultTags.and(authenticationTypeLabel)
    }

    override fun supportsContext(context: Observation.Context): Boolean {
        return context is ServerRequestObservationContext
    }

    override fun getName(): String {
        return defaultConvention.name
    }
}