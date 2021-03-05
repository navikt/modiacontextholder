package no.nav.sbl.consumers.pdl

import com.expediagroup.graphql.client.GraphQLClient
import com.expediagroup.graphql.types.GraphQLError
import com.expediagroup.graphql.types.GraphQLResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.CIOEngineConfig
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.util.KtorExperimentalAPI
import no.nav.common.log.MDCConstants
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.net.URL
import java.util.*

typealias HeadersBuilder = HttpRequestBuilder.() -> Unit
typealias VariablesTransform = (Any?) -> Any?

@KtorExperimentalAPI
class PdlClient(
    url: URL,
    private val transformVariables: VariablesTransform? = null
) : GraphQLClient<CIOEngineConfig>(url, CIO, jacksonObjectMapper(), {}) {
    private val log = LoggerFactory.getLogger(PdlClient::class.java)

    override suspend fun <T> execute(
        query: String,
        operationName: String?,
        variables: Any?,
        resultType: Class<T>,
        requestBuilder: HeadersBuilder
    ): GraphQLResponse<T> {
        val callId = getCallId()
        return try {
            val mappedVariables = transformVariables?.invoke(variables) ?: variables
            val mappedRequestBuilder: HeadersBuilder = {
                requestBuilder.invoke(this)
                header("Nav-Call-Id", callId)
            }

            super.execute(query, operationName, mappedVariables, resultType, mappedRequestBuilder)
        } catch (exception: Exception) {
            log.error("Feilet ved oppslag mot PDL (ID: $callId)", exception)
            val error = GraphQLError("Feilet ved oppslag mot PDL (ID: $callId)")
            GraphQLResponse(errors = listOf(error))
        }
    }

    private fun getCallId(): String = MDC.get(MDCConstants.MDC_CALL_ID) ?: UUID.randomUUID().toString()
}
