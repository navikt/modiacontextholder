package no.nav.sbl.rest;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.auth.subject.SubjectHandler;
import no.nav.sbl.db.DatabaseCleanerService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static no.nav.common.utils.StringUtils.notNullOrEmpty;


@Slf4j
public class CleanupServlet extends HttpServlet {

    private final DatabaseCleanerService databaseCleanerService;

    public CleanupServlet(DatabaseCleanerService databaseCleanerService) {
        this.databaseCleanerService = databaseCleanerService;
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ident = SubjectHandler.getIdent().orElse(null);
        if (notNullOrEmpty(ident)) {
            log.info("{} sletter context", ident);
            databaseCleanerService.slettAlleNyAktivBrukerEvents();
            databaseCleanerService.slettAlleUtenomSisteNyAktivEnhet();
        } else {
            resp.getWriter().write("not authorized");
            resp.setStatus(401);
        }
    }
}
