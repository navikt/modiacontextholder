package no.nav.sbl.util

import no.nav.sbl.naudit.tjenestekallLogg
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
        append("Authorization Header: ")
        appendLine(request.getHeader("Authorization").isNullOrEmpty().not())
        append("Cookies: ")
        appendLine(
            request.cookies?.joinToString(", ") {
                it.name
            }
        )
        append("Referer: ")
        appendLine(request.getHeader("Referer"))
    }
}
