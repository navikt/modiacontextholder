package no.nav.sbl.util

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

class RewriteContextPathFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (request.requestURI.startsWith("${request.contextPath}/modiacontextholder")) {
            val newUri = request.requestURI.replaceFirst("/modiacontextholder", "")
            request.getRequestDispatcher(newUri).forward(request, response)
        } else {
            filterChain.doFilter(request, response)
        }
    }
}
