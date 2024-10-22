package no.nav.modiacontextholder.config

import io.ktor.http.*
import io.ktor.server.auth.jwt.*
import no.nav.common.utils.EnvironmentUtils
import no.nav.personoversikt.common.ktor.utils.Security
import no.nav.personoversikt.common.ktor.utils.Security.AuthProviderConfig
import no.nav.personoversikt.common.utils.EnvUtils.getConfig
import no.nav.personoversikt.common.utils.EnvUtils.getRequiredConfig

private val defaultValues =
    mapOf(
        "AZURE_APP_WELL_KNOWN_URL" to "",
        "REDIS_URI_CONTEXTHOLDER" to "redis://localhost:6379/0",
        "REDIS_URI_CONTEXTHOLDER_CACHE" to "redis://localhost:6379/1",
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
            useJWTPrincipal = true,
        ),
    val isMock: Boolean = false,
    val redisUri: String = getRequiredConfig("REDIS_URI_CONTEXTHOLDER", defaultValues),
    val redisUsername: String? = getConfig("REDIS_USERNAME_CONTEXTHOLDER"),
    val redisPassword: String? = getConfig("REDIS_PASSWORD_CONTEXTHOLDER"),
    val redisCacheUri: String = getRequiredConfig("REDIS_URI_CONTEXTHOLDER_CACHE", defaultValues),
    val redisCacheUsername: String? = getConfig("REDIS_USERNAME_CONTEXTHOLDER_CACHE"),
    val redisCachePassword: String? = getConfig("REDIS_PASSWORD_CONTEXTHOLDER_CACHE"),
    val aaRegisteretBaseUrl: String = if (isMock) "ignored" else EnvironmentUtils.getRequiredProperty("AAREG_URL"),
    val aaRegisteretPublicUrl: String = if (isMock) "ignored" else EnvironmentUtils.getRequiredProperty("AAREG_PUBLIC_URL"),
    val salesforceBaseUrl: String = if (isMock) "ignored" else EnvironmentUtils.getRequiredProperty("SALESFORCE_URL"),
)
