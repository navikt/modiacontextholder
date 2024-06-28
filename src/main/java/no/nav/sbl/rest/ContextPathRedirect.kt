package no.nav.sbl.rest

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

@RestController
class ContextPathRedirect {

    @RequestMapping("/modiacontextholder/**")
    fun redirectWithUsingRedirectView(request: HttpServletRequest): RedirectView {
        val requestUri = request.requestURI
        val newUri = requestUri.replaceFirst("/modiacontextholder", "")
        return RedirectView(newUri)
    }
}