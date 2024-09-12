package no.nav.modiacontextholder.service

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.vavr.control.Try
import kotlinx.coroutines.runBlocking
import no.nav.common.client.utils.CacheUtils
import no.nav.common.utils.EnvironmentUtils
import no.nav.modiacontextholder.consumers.pdl.HeadersBuilder
import no.nav.modiacontextholder.consumers.pdl.PdlClient
import no.nav.modiacontextholder.consumers.pdl.generated.HentIdent
import no.nav.modiacontextholder.utils.BoundedMachineToMachineTokenClient
import no.nav.modiacontextholder.utils.HTTPException
import java.net.URL
import java.time.Duration

val pdlApiUrl = EnvironmentUtils.getRequiredProperty("PDL_API_URL").let(::URL)
const val AUTHORIZATION = "Authorization"
const val TEMA_HEADER = "Tema"
const val ALLE_TEMA_HEADERVERDI = "GEN"
const val AUTH_SEPERATOR = " "
const val AUTH_METHOD_BEARER = "Bearer"

class PdlService(
    private val machineToMachineTokenClient: BoundedMachineToMachineTokenClient,
) {
    private val hentIdentCache: Cache<String, Try<String>> =
        Caffeine
            .newBuilder()
            .expireAfterWrite(Duration.ofMinutes(30))
            .maximumSize(10000)
            .build()
    private val graphQLClient = PdlClient(url = pdlApiUrl)

    fun hentIdent(fnr: String): Try<String> =
        CacheUtils.tryCacheFirst(
            hentIdentCache,
            fnr,
        ) { hentIdentFraPDL(fnr) }

    fun hentIdentFraPDL(fnr: String): Try<String> =
        Try.of {
            runBlocking {
                val response = graphQLClient.execute(HentIdent(HentIdent.Variables(fnr)), systemTokenAuthorizationHeaders)
                if (response.errors != null) {
                    throw HTTPException(HttpStatusCode.BadRequest, response.errors.toString())
                }
                response
                    .data
                    ?.hentIdenter
                    ?.identer
                    ?.first()
                    ?.ident
                    ?: throw HTTPException(HttpStatusCode.NotFound, "AktørId for $fnr ble ikke funnet")
            }
        }

    private val systemTokenAuthorizationHeaders: HeadersBuilder = {
        val systemuserToken: String = machineToMachineTokenClient.createMachineToMachineToken()
        header(AUTHORIZATION, AUTH_METHOD_BEARER + AUTH_SEPERATOR + systemuserToken)
        header(TEMA_HEADER, ALLE_TEMA_HEADERVERDI)
    }
}
