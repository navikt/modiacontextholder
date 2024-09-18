package no.nav.modiacontextholder.mock

import io.vavr.control.Try
import no.nav.modiacontextholder.service.PdlService

class MockPdlService : PdlService {
    override fun hentIdent(fnr: String): Try<String> = Try.success("Z999999")
}
