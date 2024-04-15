package no.nav.sbl.rest;

import lombok.extern.slf4j.Slf4j;
import no.nav.sbl.db.DatabaseCleanerService;
import no.nav.sbl.service.AuthContextService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;


@Slf4j
public class CleanupServlet extends HttpServlet {

    private final DatabaseCleanerService databaseCleanerService;
    private final AuthContextService authContextService;

    public CleanupServlet(DatabaseCleanerService databaseCleanerService, AuthContextService authContextService) {
        this.databaseCleanerService = databaseCleanerService;
        this.authContextService = authContextService;
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<String> ident = authContextService.getIdent();
        if (ident.isPresent()) {
            log.info("{} sletter context", ident);
            databaseCleanerService.slettAlleNyAktivBrukerEvents();
            databaseCleanerService.slettAlleUtenomSisteNyAktivEnhet();
        } else {
            resp.getWriter().write("not authorized");
            resp.setStatus(401);
        }
    }
}
