package no.nav.sbl.util

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest


class RewriteContextPathFilter: Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        if (httpRequest.pathInfo.startsWith("/modiacontextholder") ) {
            val newUri = httpRequest.requestURI.replaceFirst("/modiacontextholder", "")
            request.getRequestDispatcher(newUri).forward(request, response)
        } else {
            chain.doFilter(request, response)
        }

    }
}