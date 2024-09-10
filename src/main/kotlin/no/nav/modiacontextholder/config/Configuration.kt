package no.nav.modiacontextholder.config

import io.ktor.http.*
import no.nav.personoversikt.common.ktor.utils.Security
import no.nav.personoversikt.common.ktor.utils.Security.AuthProviderConfig
import no.nav.personoversikt.common.utils.EnvUtils.getRequiredConfig

private val defaultValues =
    mapOf(
        "AZURE_APP_WELL_KNOWN_URL" to "",
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
)
