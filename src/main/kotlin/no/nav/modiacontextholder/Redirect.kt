package no.nav.modiacontextholder

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.common.rest.client.RestClient
import no.nav.modiacontextholder.config.Configuration
import no.nav.modiacontextholder.rest.model.RSContext
import no.nav.modiacontextholder.service.ContextService
import no.nav.modiacontextholder.utils.getIdent
import no.nav.personoversikt.common.ktor.utils.Security
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.ktor.ext.inject

fun Application.setupRedirect(security: Security) {
    val client: OkHttpClient = RestClient.baseClient()

    val contextService: ContextService by inject()
    val configuration: Configuration by inject()

    fun getContext(ident: String?) =
        ident?.let {
            contextService.hentVeiledersContext(it)
        }

    fun aaRegisteretUrl(context: RSContext?): String {
        val aktivBruker: String = context?.aktivBruker ?: return configuration.aaRegisteretPublicUrl
        return runCatching {
            val request =
                Request
                    .Builder()
                    .url("${configuration.aaRegisteretBaseUrl}/api/v2/redirect/sok/arbeidstaker")
                    .addHeader("Nav-Personident", aktivBruker)
                    .build()
            val response =
                client
                    .newCall(request)
                    .execute()

            check(response.isSuccessful) {
                "ResponseCode: ${response.code}"
            }
            val body =
                checkNotNull(response.body) {
                    "Body: <null>"
                }
            body.string()
        }.fold(
            onSuccess = { it },
            onFailure = { exception ->
                log.error("[AAREG] feil ved henting av aareg url. Returnerer baseurl", exception)
                configuration.aaRegisteretBaseUrl
            },
        )
    }

    routing {
        authenticate(*security.authproviders, optional = true) {
            route("redirect") {
                /**
                 * Get redirect URL (with context) to aaregisteret
                 *
                 * @OpenAPITag redirect
                 */
                get("aaregisteret") {
                    val ident = runCatching { call.getIdent() }.getOrNull()
                    call.respondRedirect(aaRegisteretUrl(getContext(ident)))
                }

                /**
                 * Get redirect URL to salesforce. (does not include context)
                 *
                 * @OpenAPITag redirect
                 */
                get("salesforce") {
                    call.respondRedirect(configuration.salesforceBaseUrl)
                }
            }
        }
    }
}
