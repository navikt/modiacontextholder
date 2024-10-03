package no.nav.modiacontextholder.utils

import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.common.token_client.client.OnBehalfOfTokenClient
import no.nav.personoversikt.common.ktor.utils.Security
import java.text.ParseException
import java.util.*

const val AAD_NAV_IDENT_CLAIM = "NAVident"

class DownstreamApi(
    val cluster: String,
    val namespace: String,
    val application: String,
) {
    companion object {
        @JvmStatic
        fun parse(value: String): DownstreamApi {
            val parts = value.split(":")
            check(parts.size == 3) { "DownstreamApi string must contain 3 parts" }

            val cluster = parts[0]
            val namespace = parts[1]
            val application = parts[2]

            return DownstreamApi(cluster = cluster, namespace = namespace, application = application)
        }
    }
}

private fun DownstreamApi.tokenscope(): String = "api://$cluster.$namespace.$application/.default"

fun MachineToMachineTokenClient.createMachineToMachineToken(api: DownstreamApi): String = this.createMachineToMachineToken(api.tokenscope())

fun OnBehalfOfTokenClient.exchangeOnBehalfOfToken(
    api: DownstreamApi,
    accesstoken: String,
): String = this.exchangeOnBehalfOfToken(api.tokenscope(), accesstoken)

interface BoundedMachineToMachineTokenClient {
    fun createMachineToMachineToken(): String
}

interface BoundedOnBehalfOfTokenClient {
    fun exchangeOnBehalfOfToken(accesstoken: String): String
}

fun MachineToMachineTokenClient.bindTo(api: DownstreamApi) =
    object : BoundedMachineToMachineTokenClient {
        override fun createMachineToMachineToken() = createMachineToMachineToken(api.tokenscope())
    }

fun MachineToMachineTokenClient.bindTo(api: String) =
    object : BoundedMachineToMachineTokenClient {
        override fun createMachineToMachineToken(): String = api
    }

fun OnBehalfOfTokenClient.bindTo(api: DownstreamApi) =
    object : BoundedOnBehalfOfTokenClient {
        override fun exchangeOnBehalfOfToken(accesstoken: String) = exchangeOnBehalfOfToken(api.tokenscope(), accesstoken)
    }

fun OnBehalfOfTokenClient.bindTo(api: String) =
    object : BoundedOnBehalfOfTokenClient {
        override fun exchangeOnBehalfOfToken(accesstoken: String) = exchangeOnBehalfOfToken(api, accesstoken)
    }

fun ApplicationCall.getIdToken(): String {
    val authHeader = this.request.parseAuthorizationHeader()
    if (authHeader != null && authHeader is HttpAuthHeader.Single && authHeader.authScheme == "Bearer") {
        return authHeader.blob
    }
    throw AuthorizationException("Missing or invalid authorization header")
}

fun ApplicationCall.getIdent(): String =
    checkNotNull(
        this.principal<JWTPrincipal>()?.getClaim(AAD_NAV_IDENT_CLAIM, String::class)
            ?: this.principal<Security.SubjectPrincipal>()?.subject,
    ) {
        "Could not find subject from JWT"
    }

fun ApplicationCall.getAuthorizedParty(): Optional<String> =
    try {
        val claim = this.principal<JWTPrincipal>()?.getClaim("azp_name", String::class)
        Optional.ofNullable(claim)
    } catch (_: ParseException) {
        Optional.empty()
    }
