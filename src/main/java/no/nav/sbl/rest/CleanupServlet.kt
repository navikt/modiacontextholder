package no.nav.sbl.rest

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.sbl.db.DatabaseCleanerService
import no.nav.sbl.service.AuthContextService
import org.slf4j.LoggerFactory
import java.io.IOException

class CleanupServlet(
    private val databaseCleanerService: DatabaseCleanerService,
    private val authContextService: AuthContextService
) : HttpServlet() {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Throws(ServletException::class, IOException::class)
    override fun doDelete(req: HttpServletRequest, resp: HttpServletResponse) {
        val ident = authContextService.ident
        if (ident.isPresent) {
            log.info("{} sletter context", ident)
            databaseCleanerService.slettAlleNyAktivBrukerEvents()
            databaseCleanerService.slettAlleUtenomSisteNyAktivEnhet()
        } else {
            resp.writer.write("not authorized")
            resp.status = 401
        }
    }
}