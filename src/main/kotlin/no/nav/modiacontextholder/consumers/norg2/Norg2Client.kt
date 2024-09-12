package no.nav.modiacontextholder.consumers.norg2

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.common.log.MDCConstants
import no.nav.common.utils.IdUtils
import no.nav.modiacontextholder.consumers.norg2.domain.Enhet
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.MDC

private val Norg2EnheterResponse = object : TypeReference<List<Enhet>>() {}

class Norg2Client(
    private val url: String,
    private val client: OkHttpClient,
) {
    private val objectmapper =
        jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun hentAlleEnheter(): List<Enhet> {
        val callId = MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()

        val response =
            client
                .newCall(
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
                        .build(),
                ).execute()

        return objectmapper.readValue(response.body?.byteStream(), Norg2EnheterResponse)
    }

    fun ping() {
        val status = client.newCall(Request.Builder().url("$url/internal/isAlive").build()).execute().code
        if (status != 200) {
            throw RuntimeException("Norg2 /isAlive status: $status")
        }
    }
}
