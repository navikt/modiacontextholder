package no.nav.sbl.service

import io.ktor.client.request.header
import io.ktor.util.KtorExperimentalAPI
import io.vavr.control.Try
import kotlinx.coroutines.runBlocking
import no.nav.common.sts.SystemUserTokenProvider
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.consumers.pdl.HeadersBuilder
import no.nav.sbl.consumers.pdl.PdlClient
import no.nav.sbl.consumers.pdl.generated.HentIdent
import java.net.URL
import javax.ws.rs.NotFoundException

val pdlApiUrl: URL = EnvironmentUtils.getRequiredProperty("PDL_API_URL").let(::URL)

@KtorExperimentalAPI
class PdlService(private val stsService: SystemUserTokenProvider) {
    private val graphQLClient = PdlClient(url = pdlApiUrl)

    fun hentIdent(fnr: String): Try<String> = Try.of {
        runBlocking {
            HentIdent(graphQLClient)
                    .execute(HentIdent.Variables(fnr), userTokenHeaders)
                    .data
                    ?.hentIdenter
                    ?.identer
                    ?.first()
                    ?.ident
                    ?: throw NotFoundException("AktørId for $fnr ble ikke funnet")
        }
    }

    private var userTokenHeaders: HeadersBuilder = {
        val systemuserToken: String = stsService.systemUserToken
        val userToken: String = AuthContextService.requireIdToken()

        header("Nav-Consumer-Token", "Bearer $systemuserToken")
        header("Authorization", "Bearer $userToken")
        header("Tema", "GEN")
    }
}
