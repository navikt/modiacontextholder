package no.nav.sbl.util

import org.slf4j.LoggerFactory
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class AuthIntrospectionFilter : Filter {
    private val log = LoggerFactory.getLogger(AuthIntrospectionFilter::class.java)

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        request as HttpServletRequest
        request.cookies.size

        val logString = buildString {
            appendLine("[AuthIntrospection]")
            append("Authorization Header: ")
            appendLine(request.getHeader("Authorization").isNullOrEmpty().not())
            append("Cookies: ")
            appendLine(
                request.cookies.joinToString(", ") {
                    it.name
                }
            )
        }
        log.info(logString)
        chain.doFilter(request, response)
    }
}
