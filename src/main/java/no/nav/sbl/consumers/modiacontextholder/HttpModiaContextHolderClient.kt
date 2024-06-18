package no.nav.sbl.consumers.modiacontextholder

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.sbl.rest.domain.RSAktivBruker
import no.nav.sbl.rest.domain.RSAktivEnhet
import no.nav.sbl.rest.domain.RSContext
import no.nav.sbl.rest.domain.RSNyContext
import no.nav.sbl.service.AuthContextService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class HttpModiaContextHolderClient(
    private val client: OkHttpClient,
    private val baseUrl: String,
    private val authContextService: AuthContextService,
    private val objectMapper: ObjectMapper,
) : ModiaContextHolderClient {
    override fun hentVeiledersContext(veilederIdent: String): Result<RSContext> = runCatching {
        val request = client.newCall(
            Request.Builder().apply {
                url("$baseUrl/api/context")
                header("Authorization", "Bearer ${authContextService.getAccessToken()}")
            }.build()
        )

        request.execute().use { response ->
            objectMapper.readValue(response.body?.byteStream(), RSContext::class.java)
        }
    }

    override fun oppdaterVeiledersContext(nyContext: RSNyContext, veilederIdent: String): Result<RSContext> =
        runCatching {
            val requestBody = objectMapper.writeValueAsString(nyContext)
            val request = client.newCall(
                Request.Builder().apply {
                    url("$baseUrl/api/context")
                    header("Authorization", "Bearer ${authContextService.getAccessToken()}")
                    getRefererHeader()?.let { referer ->
                        header("referer", referer)
                    }
                    post(requestBody.toRequestBody("application/json".toMediaType()))
                }.build()
            )

            request.execute().use { response ->
                objectMapper.readValue(response.body?.byteStream(), RSContext::class.java)
            }
        }

    override fun hentAktivBruker(veilederIdent: String): Result<RSContext> = runCatching {

        val request = client.newCall(
            Request.Builder().apply {
                url("$baseUrl/api/context/aktivbruker")
                header("Authorization", "Bearer ${authContextService.getAccessToken()}")
            }.build()
        )

        request.execute().use { response ->
            objectMapper.readValue(response.body?.byteStream(), RSContext::class.java)
        }
    }

    override fun hentAktivBrukerV2(veilederIdent: String): Result<RSAktivBruker> = runCatching {
        val request = client.newCall(
            Request.Builder().apply {
                url("$baseUrl/api/context/v2/aktivbruker")
                header("Authorization", "Bearer ${authContextService.getAccessToken()}")
            }.build()
        )

        request.execute().use { response ->
            objectMapper.readValue(response.body?.byteStream(), RSAktivBruker::class.java)
        }
    }

    override fun hentAktivEnhet(veilederIdent: String): Result<RSContext> = runCatching {
        val request = client.newCall(
            Request.Builder().apply {
                url("$baseUrl/api/context/aktivenhet")
                header("Authorization", "Bearer ${authContextService.getAccessToken()}")
            }.build()
        )
        request.execute().use { response ->
            objectMapper.readValue(response.body?.byteStream(), RSContext::class.java)
        }
    }

    override fun hentAktivEnhetV2(veilederIdent: String): Result<RSAktivEnhet> = runCatching {
        val request = client.newCall(
            Request.Builder().apply {
                url("$baseUrl/api/context/v2/aktivenhet")
                header("Authorization", "Bearer ${authContextService.getAccessToken()}")
            }.build()
        )

        request.execute().use { response ->
            objectMapper.readValue(response.body?.byteStream(), RSAktivEnhet::class.java)
        }
    }

    override fun nullstillBrukerContext(veilederident: String): Result<Unit> = runCatching {
        val request = client.newCall(
            Request.Builder().apply {
                url("$baseUrl/api/context")
                header("Authorization", "Bearer ${authContextService.getAccessToken()}")
                getRefererHeader()?.let { referer ->
                    header("referer", referer)
                }
                delete()
            }.build()
        )

        request.execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to nullstillContext")
            }
        }

    }

    override fun nullstillAktivBruker(veilederIdent: String): Result<Unit> = runCatching {
        val request = client.newCall(
            Request.Builder().apply {
                url("$baseUrl/api/context/aktivbruker")
                header("Authorization", "Bearer ${authContextService.getAccessToken()}")
                getRefererHeader()?.let { referer ->
                    header("referer", referer)
                }
                delete()
            }.build()
        )

        request.execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to nullstillAktivBruker")
            }
        }
    }

    private fun getRefererHeader(): String? =
        (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request?.getHeader("referer")
}