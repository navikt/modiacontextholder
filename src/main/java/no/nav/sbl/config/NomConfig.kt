package no.nav.sbl.config

import no.nav.common.client.nom.NomClient
import no.nav.common.client.nom.NomClientImpl
import no.nav.common.client.nom.VeilederNavn
import no.nav.common.health.HealthCheckResult
import no.nav.common.rest.client.RestClient
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.common.types.identer.NavIdent
import no.nav.common.utils.EnvironmentUtils
import no.nav.sbl.util.DownstreamApi
import no.nav.sbl.util.LoggingInterceptor
import no.nav.sbl.util.createMachineToMachineToken
import no.nav.sbl.util.getCallId
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class NomConfig {
    private val scope = DownstreamApi.parse(EnvironmentUtils.getRequiredProperty("NOM_SCOPE"))
    private val url: String = EnvironmentUtils.getRequiredProperty("NOM_URL")
    private val httpClient: OkHttpClient =
        RestClient
            .baseClient()
            .newBuilder()
            .addInterceptor(
                LoggingInterceptor("Nom") {
                    getCallId()
                },
            ).build()

    @Autowired
    lateinit var tokenProvider: MachineToMachineTokenClient

    @Bean
    open fun nomClient(): NomClient {
        if (EnvironmentUtils.isDevelopment().orElse(false)) {
            return DevNomClient()
        }
        val tokenSupplier = { tokenProvider.createMachineToMachineToken(scope) }
        return NomClientImpl(url, tokenSupplier, httpClient)
    }
}

private class DevNomClient : NomClient {
    override fun checkHealth(): HealthCheckResult = HealthCheckResult.healthy()

    override fun finnNavn(navIdent: NavIdent): VeilederNavn = lagVeilederNavn(navIdent)

    override fun finnNavn(identer: MutableList<NavIdent>): List<VeilederNavn> = identer.map(::lagVeilederNavn)

    private fun lagVeilederNavn(navIdent: NavIdent): VeilederNavn {
        val ident = navIdent.get()
        val identNr = ident.substring(1)
        return VeilederNavn()
            .setNavIdent(navIdent)
            .setFornavn("F_$identNr")
            .setEtternavn("E_$identNr")
            .setVisningsNavn("F_$identNr E_$identNr")
    }
}
