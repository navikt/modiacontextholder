package no.nav.sbl.consumers.pdl

import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import com.expediagroup.graphql.client.types.GraphQLClientError
import com.expediagroup.graphql.client.types.GraphQLClientRequest
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import io.ktor.client.request.*
import no.nav.common.log.MDCConstants
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.net.URL
import java.util.*

typealias HeadersBuilder = HttpRequestBuilder.() -> Unit

class PdlClient(
    url: URL,
) : GraphQLKtorClient(url) {
    private val log = LoggerFactory.getLogger(PdlClient::class.java)

    override suspend fun <T : Any> execute(
        request: GraphQLClientRequest<T>,
        requestCustomizer: HttpRequestBuilder.() -> Unit
    ): GraphQLClientResponse<T> {
        val callId = getCallId()
        return try {
            val mappedRequestBuilder: HeadersBuilder = {
                requestCustomizer.invoke(this)
                header("Nav-Call-Id", callId)
            }

            super.execute(request, mappedRequestBuilder)
        } catch (exception: Exception) {
            log.error("Feilet ved oppslag mot PDL (ID: $callId)", exception)
            val error = GenericGraphQlError("Feilet ved oppslag mot PDL (ID: $callId)")
            GenericGraphQlResponse(errors = listOf(error))
        }
    }

    private fun getCallId(): String = MDC.get(MDCConstants.MDC_CALL_ID) ?: UUID.randomUUID().toString()

    data class GenericGraphQlResponse<T>(
        override val errors: List<GraphQLClientError>? = null,
        override val data: T? = null,
    ) : GraphQLClientResponse<T>

    data class GenericGraphQlError(
        override val message: String,
    ) : GraphQLClientError
}
