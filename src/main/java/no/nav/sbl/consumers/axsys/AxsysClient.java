package no.nav.sbl.consumers.axsys;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Option;
import lombok.SneakyThrows;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.selftest.SelfTestCheck;
import no.nav.common.log.MDCConstants;
import no.nav.common.rest.client.RestClient;
import no.nav.common.utils.IdUtils;
import no.nav.sbl.config.Pingable;
import no.nav.sbl.consumers.axsys.domain.AxsysTilgangResponse;
import no.nav.sbl.consumers.axsys.domain.HttpRequestConstants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.MDC;

public class AxsysClient {
    private final String systemUser;
    private final String url;
    private final OkHttpClient client = RestClient.baseClient();
    private final ObjectMapper objectmapper = new ObjectMapper();

    public AxsysClient(String axsysUrl, String systemUser) {
        this.url = axsysUrl;
        this.systemUser = systemUser;
    }

    @SneakyThrows
    public AxsysTilgangResponse hentTilgang(String ident) {
        String callId = Option.of(MDC.get(MDCConstants.MDC_CALL_ID)).getOrElse(IdUtils::generateId);
        String consumerId = Option.of(MDC.get(MDCConstants.MDC_CONSUMER_ID)).getOrElse(systemUser);

        Response response = client
                .newCall(new Request.Builder()
                        .url(url + "/api/v1/tilgang/" + ident)
                        .header(HttpRequestConstants.HEADER_NAV_CALL_ID, callId)
                        .header(HttpRequestConstants.HEADER_NAV_CONSUMER_ID, consumerId)
                        .header("Content-Type", "application/json")
                        .build()
                )
                .execute();
        return objectmapper.readValue(response.body().byteStream(), AxsysTilgangResponse.class);
    }

    @SneakyThrows
    public void ping() {
        int status = client.newCall(new Request.Builder().url("${url}/internal/isAlive").build()).execute().code();
        if (status != 200) {
            throw new RuntimeException("Axsys /isAlive status: " + status);
        }
    }
}
