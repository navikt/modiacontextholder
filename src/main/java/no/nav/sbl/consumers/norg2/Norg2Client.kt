package no.nav.sbl.consumers.norg2

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.common.log.MDCConstants
import no.nav.common.rest.client.RestClient
import no.nav.common.utils.IdUtils
import no.nav.sbl.consumers.axsys.domain.HttpRequestConstants
import no.nav.sbl.consumers.norg2.domain.Enhet
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.MDC

private val Norg2EnheterResponse = object : TypeReference<List<Enhet>>() {}

class Norg2Client(private val url: String, private val systemUser: String) {
    private val objectmapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    private val client: OkHttpClient = RestClient.baseClient()

    fun hentAlleEnheter(): List<Enhet> {
        val callId = MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()
        val consumerId = MDC.get(MDCConstants.MDC_CONSUMER_ID) ?: systemUser

        val response = client
            .newCall(
                Request.Builder()
                    .url(
                        HttpUrl
                            .get("$url/api/v1/enhet")
                            .newBuilder()
                            .addQueryParameter("enhetStatusListe", "AKTIV")
                            .addQueryParameter("enhetStatusListe", "UNDER_ETABLERING")
                            .addQueryParameter("enhetStatusListe", "UNDER_AVVIKLING")
                            .build()
                    )
                    .header(HttpRequestConstants.HEADER_NAV_CALL_ID, callId)
                    .header(HttpRequestConstants.HEADER_NAV_CONSUMER_ID, consumerId)
                    .header("Content-Type", "application/json")
                    .build()
            ).execute()

        return objectmapper.readValue(response.body()?.byteStream(), Norg2EnheterResponse)
    }

    fun ping() {
        val status = client.newCall(Request.Builder().url("$url/internal/isAlive").build()).execute().code()
        if (status != 200) {
            throw RuntimeException("Norg2 /isAlive status: $status")
        }
    }
}
