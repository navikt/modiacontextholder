package no.nav.sbl.util

import com.nimbusds.jwt.JWTParser
import no.nav.sbl.naudit.tjenestekallLogg
import java.lang.Exception
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class AuthIntrospectionFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        request as HttpServletRequest

        tjenestekallLogg.info(buildLogMessage(request))

        chain.doFilter(request, response)
    }

    private fun buildLogMessage(request: HttpServletRequest) = buildString {
        appendLine("[AuthIntrospection]")

        val bearerToken = request.getHeader("Authorization")?.removePrefix("Bearer ")
        if (bearerToken != null) {
            try {
                val parsedJWT = JWTParser.parse(bearerToken)
                append("Audience: ")
                appendLine(parsedJWT.jwtClaimsSet.audience)
            } catch (_: Exception) {
                // Pass
            }
        }

        val origin = request.getHeader("Origin")
        if (origin != null) {
            append("Origin: ")
            appendLine(origin)
        }
    }
}
