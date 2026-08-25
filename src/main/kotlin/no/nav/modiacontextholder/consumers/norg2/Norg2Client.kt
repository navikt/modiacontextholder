package no.nav.modiacontextholder.consumers.norg2

import kotlinx.serialization.json.Json
import no.nav.common.health.HealthCheckResult
import no.nav.common.health.selftest.SelfTestCheck
import no.nav.common.log.MDCConstants
import no.nav.common.utils.IdUtils
import no.nav.modiacontextholder.consumers.norg2.domain.Enhet
import no.nav.modiacontextholder.infrastructur.HealthCheckAware
import no.nav.personoversikt.common.utils.SelftestGenerator
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.MDC

interface Norg2Client {
    fun hentAlleEnheter(): List<Enhet>
}

class Norg2ClientImpl(
    private val url: String,
    private val client: OkHttpClient,
) : Norg2Client,
    HealthCheckAware {
    private val json = Json { ignoreUnknownKeys = true }

    private val reporter = SelftestGenerator.Reporter(name = "Norg2 client", critical = true)

    override fun hentAlleEnheter(): List<Enhet> {
        val callId = MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()

        val request =
            Request
                .Builder()
                .url(
                    "$url/api/v1/enhet"
                        .toHttpUrl()
                        .newBuilder()
                        .addQueryParameter("enhetStatusListe", "AKTIV")
                        .addQueryParameter("enhetStatusListe", "UNDER_ETABLERING")
                        .addQueryParameter("enhetStatusListe", "UNDER_AVVIKLING")
                        .build(),
                ).header("Nav-Call-Id", callId)
                .header("Content-Type", "application/json")
                .build()

        val body =
            client
                .newCall(request)
                .execute()
                .use { response ->
                    if (!response.isSuccessful) {
                        throw RuntimeException("Norg2 /api/v1/enhet svarte ${response.code} ${response.message}")
                    }
                    response.body.string()
                }

        return json.decodeFromString<List<Enhet>>(body)
    }

    override fun getHealthCheck(): SelfTestCheck =
        SelfTestCheck(
            "NORG2 client",
            true,
        ) { checkHealth() }

    private fun checkHealth(): HealthCheckResult {
        try {
            ping()
            reporter.reportOk()
            return HealthCheckResult.healthy()
        } catch (e: RuntimeException) {
            reporter.reportError(e)
            return HealthCheckResult.unhealthy(e.cause)
        }
    }

    fun ping() {
        val status =
            client
                .newCall(Request.Builder().url("$url/internal/health/liveness").build())
                .execute()
                .use { it.code }
        if (status != 200) {
            throw RuntimeException("Norg2 /isAlive status: $status")
        }
    }
}
