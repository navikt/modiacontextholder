package no.nav.sbl.rest

import no.nav.common.rest.client.RestClient
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.rest.domain.RSContext
import no.nav.sbl.service.AuthContextService
import no.nav.sbl.service.ContextService
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/redirect")
class RedirectRessurs @Autowired constructor(
    private val authContextUtils: AuthContextService,
    private val contextService: ContextService
) {
    private val client: OkHttpClient = RestClient.baseClient()
    private val log = LoggerFactory.getLogger(RedirectRessurs::class.java)

    @GetMapping("/aaregisteret")
    fun aaRegisteret(): ResponseEntity<Unit> {
        return temporaryRedirect(aaRegisteretUrl(aktivContext()))
    }

    private val aaRegisteretBaseUrl = EnvironmentUtils.getRequiredProperty("AAREG_URL")
    private fun aaRegisteretUrl(context: RSContext?): String {
        val aktivBruker: String = context?.aktivBruker ?: return aaRegisteretBaseUrl
        return try {
            val request = Request.Builder()
                .url("$aaRegisteretBaseUrl/api/v2/redirect/sok/arbeidstaker")
                .addHeader("Nav-Personident", aktivBruker)
                .build()
            val response = client
                .newCall(request)
                .execute()

            requireNotNull(response.body())
                .string()
                .also {
                    log.info("[AAREG] Byttet ut brukercontext for url: $it")
                }
        } catch (e: Throwable) {
            aaRegisteretBaseUrl.also {
                log.error("[AAREG] Bytte av brukercontext til url feilet", e)
            }
        }
    }

    private fun aktivContext(): RSContext? {
        return authContextUtils.ident
            .map(contextService::hentVeiledersContext)
            .orElse(null)
    }

    private fun temporaryRedirect(url: String) = ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(url))
        .build<Unit>()
}
