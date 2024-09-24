package no.nav.modiacontextholder.config

import io.ktor.http.*
import no.nav.personoversikt.common.ktor.utils.Security
import no.nav.personoversikt.common.ktor.utils.Security.AuthProviderConfig
import no.nav.personoversikt.common.utils.EnvUtils.getConfig
import no.nav.personoversikt.common.utils.EnvUtils.getRequiredConfig

private val defaultValues =
    mapOf(
        "AZURE_APP_WELL_KNOWN_URL" to "",
        "REDIS_URI_CONTEXTHOLDER" to "redis://localhost:6379/0",
    )

class Configuration(
    val azuread: AuthProviderConfig =
        AuthProviderConfig(
            name = "azuread",
            jwksConfig =
                Security.JwksConfig.OidcWellKnownUrl(
                    getRequiredConfig(
                        "AZURE_APP_WELL_KNOWN_URL",
                        defaultValues,
                    ),
                ),
            tokenLocations =
                listOf(
                    Security.TokenLocation.Header(HttpHeaders.Authorization),
                ),
        ),
    val redisUri: String = getRequiredConfig("REDIS_URI_CONTEXTHOLDER", defaultValues),
    val redisUsername: String? = getConfig("REDIS_USERNAME_CONTEXTHOLDER"),
    val redisPassword: String? = getConfig("REDIS_PASSWORD_CONTEXTHOLDER"),
)
