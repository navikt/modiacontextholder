package no.nav.sbl.service

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.client.request.*
import io.ktor.util.*
import io.vavr.control.Try
import kotlinx.coroutines.runBlocking
import no.nav.common.client.utils.CacheUtils
import no.nav.common.sts.SystemUserTokenProvider
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.consumers.pdl.HeadersBuilder
import no.nav.sbl.consumers.pdl.PdlClient
import no.nav.sbl.consumers.pdl.generated.HentIdent
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.net.URL
import java.time.Duration

val pdlApiUrl: URL = EnvironmentUtils.getRequiredProperty("PDL_API_URL").let(::URL)

@KtorExperimentalAPI
class PdlService(private val stsService: SystemUserTokenProvider) {
    private val hentIdentCache: Cache<String, Try<String>> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(30))
        .maximumSize(10000)
        .build()
    private val graphQLClient = PdlClient(url = pdlApiUrl)

    fun hentIdent(fnr: String) {
            CacheUtils.tryCacheFirst(hentIdentCache, fnr
            ) { hentIdentFraPDL(fnr) }
    }

    fun hentIdentFraPDL(fnr: String): Try<String> = Try.of {
        runBlocking {
            val response = HentIdent(graphQLClient).execute(HentIdent.Variables(fnr), systemTokenHeaders)
            if (response.errors != null) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, response.errors.toString())
            }
            response
                .data
                ?.hentIdenter
                ?.identer
                ?.first()
                ?.ident
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "AktørId for $fnr ble ikke funnet")
        }
    }


    private var systemTokenHeaders: HeadersBuilder = {
        val systemuserToken: String = stsService.systemUserToken

        header("Nav-Consumer-Token", "Bearer $systemuserToken")
        header("Authorization", "Bearer $systemuserToken")
        header("Tema", "GEN")
    }

}
