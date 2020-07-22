package no.nav.sbl.rest

import com.expediagroup.graphql.client.GraphQLClient
import com.expediagroup.graphql.types.GraphQLResponse
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.vavr.control.Try
import kotlinx.coroutines.runBlocking
import no.nav.common.auth.SsoToken
import no.nav.common.auth.SubjectHandler
import no.nav.common.oidc.SystemUserTokenProvider
import no.nav.sbl.pdl.generated.HentIdent
import no.nav.sbl.util.EnvironmentUtils
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*
import javax.ws.rs.NotFoundException

typealias HeadersBuilder = HttpRequestBuilder.() -> Unit
private data class RequestContext<V>(val fnr: V, val headers: HeadersBuilder)

class PdlService(private val stsService: SystemUserTokenProvider) {
    private val log = LoggerFactory.getLogger(PdlService::class.java)
    private val graphQLClient = GraphQLClient(
            url = URL(getEnvironmentUrl())
    )

    fun hentIdent(fnr: String): Try<String> = Try.of {
        prepareRequest(fnr)
                .executeRequest { HentIdent(graphQLClient).execute(HentIdent.Variables(it.fnr), it.headers) }
                ?.data
                ?.hentIdenter
                ?.identer
                ?.first()
                ?.ident
                ?: throw NotFoundException("AktørId for $fnr ble ikke funnet")
    }

    private fun prepareRequest(fnr: String) = RequestContext(fnr, userTokenHeaders)

    private fun <T, V> RequestContext<V>.executeRequest(request: suspend (RequestContext<V>) -> GraphQLResponse<T>): GraphQLResponse<T>? {
        val context = this
        return try {
            runBlocking {
                val response: GraphQLResponse<T> = request(context)
                if (response.errors.isNullOrEmpty()) {
                    return@runBlocking response
                } else {
                    log.error("Feilet ved oppslag mot PDL (fnr: ${context.fnr}):\n{}", response.errors)
                    return@runBlocking null
                }
            }
        } catch (exception: Exception) {
            log.error("Feilet ved oppslag mot PDL (fnr: ${context.fnr})", exception)
            return null
        }
    }

    private var userTokenHeaders : HeadersBuilder = {
        val systemuserToken: String = stsService.systemUserAccessToken
        val userToken: String = SubjectHandler.getSsoToken(SsoToken.Type.OIDC).orElseThrow { IllegalStateException("Kunne ikke hente ut veileders ssoTOken") }

        header("Nav-Call-Id", UUID.randomUUID())
        header("Nav-Consumer-Token", "Bearer $systemuserToken")
        header("Authorization", "Bearer $userToken")
        header("Tema", "GEN")
    }
}

private fun getEnvironmentUrl(): String {
    return if ("p" == EnvironmentUtils.getRequiredProperty("APP_ENVIRONMENT_NAME").toLowerCase()) {
        "https://pdl-api.nais.adeo.no/graphql"
    } else {
        "https://pdl-api.nais.preprod.local/graphql"
    }
}

