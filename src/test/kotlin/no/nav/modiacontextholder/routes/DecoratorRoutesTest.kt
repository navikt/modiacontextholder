package no.nav.modiacontextholder.routes

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.every
import kotlinx.serialization.json.Json
import no.nav.common.types.identer.AzureObjectId
import no.nav.modiacontextholder.rest.FnrRequest
import no.nav.modiacontextholder.rest.TestApplication
import no.nav.modiacontextholder.rest.model.DecoratorDomain
import no.nav.modiacontextholder.service.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.koin.test.inject
import org.koin.test.mock.declareMock
import kotlin.test.Test

class DecoratorRoutesTest : TestApplication() {
    private val ident = "Z999999"
    private val adminGroupName = "0000-GA-Modia_Admin"
    private val regionalGroupName = "0000-GA-GOSYS_REGIONAL"
    private val azureObjectId = AzureObjectId("d2987104-63b2-4110-83ac-20ff6afe24a2").get()

    private val enheterService: EnheterService by lazy { declareMock<EnheterService>() }
    private val veilederService: VeilederService by lazy { declareMock<VeilederService>() }

    @Test
    fun `ingen saksbehandlerident`() =
        testApp {
            gitt_saksbehandler_i_ad()
            client.get("/api/v2/decorator").apply {
                assertEquals(HttpStatusCode.Forbidden, this.status)
                assert(this.bodyAsText().contains("Missing or invalid authorization header"))
            }
        }

    @Test
    fun feil_ved_henting_av_enheter() =
        testApp {
            gitt_saksbehandler_i_ad()

            coEvery { enheterService.hentEnheter(ident, any(), any()) } returns Result.failure(IllegalStateException("Noe gikk feil"))

            client.getAuth("/api/v2/decorator").apply {
                assertEquals(this.status, HttpStatusCode.InternalServerError)
                assert(this.bodyAsText().contains("Kunne ikke hente data"))
            }
        }

    @Test
    fun returnerer_saksbehandlernavn() =
        testApp {
            gitt_saksbehandler_i_ad()
            gitt_tilgang_til_enheter(emptyList())

            client.getAuth("/api/v2/decorator").apply {
                val decoratorConfig = Json.decodeFromString<DecoratorDomain.DecoratorConfig>(this.bodyAsText())

                assertThat(decoratorConfig.ident).isEqualTo(ident)
                assertThat(decoratorConfig.fornavn).isEqualTo("Fornavn")
                assertThat(decoratorConfig.etternavn).isEqualTo("Etternavn")
            }
        }

    @Test
    fun bare_aktive_enheter() =
        testApp {
            gitt_saksbehandler_i_ad()
            gitt_tilgang_til_enheter(
                listOf(
                    enhet("0001", "Test 1", "LOKAL"),
                    enhet("0002", "Test 2", "KLAGE"),
                    enhet("0003", "Test 3", null),
                ),
            )

            client.getAuth("/api/v2/decorator").apply {
                val decoratorConfig = Json.decodeFromString<DecoratorDomain.DecoratorConfig>(this.bodyAsText())
                assertThat(decoratorConfig.enheter).hasSize(3)
            }
        }

    @Test
    fun alle_enheter_om_saksbehandler_har_modia_admin() =
        testApp {
            val azureADService = declareMock<AzureADService>()
            coEvery { azureADService.fetchRoller(any(), any()) } returns
                listOf(
                    AnsattRolle(
                        adminGroupName,
                        azureObjectId,
                    ),
                )

            gitt_saksbehandler_i_ad()
            gitt_tilgang_til_enheter(
                listOf(
                    enhet("0001", "Test 1", "LOKAL"),
                ),
            )

            client.getAuth("/api/v2/decorator").apply {
                val decoratorConfig = Json.decodeFromString<DecoratorDomain.DecoratorConfig>(this.bodyAsText())
                assertThat(decoratorConfig.enheter).hasSize(5)
            }
        }

    @Test
    fun `opt-in gir kun oppgavebehandler-enheter for vanlig veileder`() =
        testApp {
            gitt_saksbehandler_i_ad()
            gitt_tilgang_til_enheter(alleEnheter)

            client.getAuth("/api/v2/decorator?kunOppgavebehandlendeEnheter=true").apply {
                val decoratorConfig = Json.decodeFromString<DecoratorDomain.DecoratorConfig>(this.bodyAsText())
                // kun 0001 og 0003 er true; 0005 er null og likestilles med false
                assertThat(decoratorConfig.enheter.map { it.enhetId }).containsExactly("0001", "0003")
            }
        }

    @Test
    fun `opt-in gjelder ogsaa for modia-admin`() =
        testApp {
            val azureADService = declareMock<AzureADService>()
            coEvery { azureADService.fetchRoller(any(), any()) } returns
                listOf(AnsattRolle(adminGroupName, azureObjectId))

            gitt_saksbehandler_i_ad()
            gitt_tilgang_til_enheter(emptyList())

            client.getAuth("/api/v2/decorator?kunOppgavebehandlendeEnheter=true").apply {
                val decoratorConfig = Json.decodeFromString<DecoratorDomain.DecoratorConfig>(this.bodyAsText())
                assertThat(decoratorConfig.enheter.map { it.enhetId }).containsExactly("0001", "0003")
            }
        }

    @Test
    fun `uten opt-in returneres alle enheter`() =
        testApp {
            gitt_saksbehandler_i_ad()
            gitt_tilgang_til_enheter(alleEnheter)

            client.getAuth("/api/v2/decorator").apply {
                val decoratorConfig = Json.decodeFromString<DecoratorDomain.DecoratorConfig>(this.bodyAsText())
                assertThat(decoratorConfig.enheter).hasSize(5)
            }
        }

    @Test
    fun `oppgavebehandler-flagget eksponeres i responsen`() =
        testApp {
            gitt_saksbehandler_i_ad()
            gitt_tilgang_til_enheter(alleEnheter)

            client.getAuth("/api/v2/decorator").apply {
                val decoratorConfig = Json.decodeFromString<DecoratorDomain.DecoratorConfig>(this.bodyAsText())
                assertThat(decoratorConfig.enheter.single { it.enhetId == "0001" }.oppgavebehandler).isTrue()
                assertThat(decoratorConfig.enheter.single { it.enhetId == "0002" }.oppgavebehandler).isFalse()
                assertThat(decoratorConfig.enheter.single { it.enhetId == "0005" }.oppgavebehandler).isNull()
            }
        }

    @Test
    fun `ugyldig verdi paa oppgavebehandler gir 400`() =
        testApp {
            gitt_saksbehandler_i_ad()
            gitt_tilgang_til_enheter(alleEnheter)

            client.getAuth("/api/v2/decorator?kunOppgavebehandlendeEnheter=kanskje").apply {
                assertEquals(HttpStatusCode.BadRequest, this.status)
            }
        }

    @Test
    fun `hent-fnr route`() =
        testApp {
            val mockPdl: PdlService by inject()

            it.postAuth("/api/decorator/aktor/hent-fnr", FnrRequest(fnr = "10108000398")).apply {
                val body = this.body<DecoratorDomain.FnrAktorId>()
                assertThat(body.aktorId).isEqualTo(mockPdl.hentIdent(body.fnr).get())
            }
        }

    private fun gitt_tilgang_til_enheter(data: List<DecoratorDomain.Enhet>) {
        coEvery { enheterService.hentEnheter(ident, any(), any()) } answers {
            Result.success(data.kunOppgavebehandlere(thirdArg()))
        }
        every { enheterService.hentAlleEnheter(any()) } answers {
            alleEnheter.kunOppgavebehandlere(firstArg())
        }
    }

    /** Speiler serverens filtrering slik at ruten kan verifiseres ende-til-ende. */
    private fun List<DecoratorDomain.Enhet>.kunOppgavebehandlere(aktivert: Boolean) =
        if (aktivert) filter { it.oppgavebehandler == true } else this

    private fun gitt_saksbehandler_i_ad() {
        coEvery { veilederService.hentVeilederNavn(any()) } returns
            DecoratorDomain.Saksbehandler(
                ident,
                "Fornavn",
                "Etternavn",
            )
    }

    private fun enhet(
        id: String,
        navn: String,
        type: String?,
        oppgavebehandler: Boolean? = null,
    ): DecoratorDomain.Enhet = DecoratorDomain.Enhet(id, navn, type, oppgavebehandler = oppgavebehandler)

    private val alleEnheter
        get() =
            listOf(
                enhet("0001", "Test 1", "LOKAL", oppgavebehandler = true),
                enhet("0002", "Test 2", "KLAGE", oppgavebehandler = false),
                enhet("0003", "Test 3", "OKONOMI", oppgavebehandler = true),
                enhet("0004", "Test 4", "KO", oppgavebehandler = false),
                enhet("0005", "Test 5", "IT", oppgavebehandler = null),
            )
}
