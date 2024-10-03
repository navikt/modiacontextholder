package no.nav.modiacontextholder.service

import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.modiacontextholder.mock.MockAzureADService
import no.nav.modiacontextholder.utils.BoundedOnBehalfOfTokenClient
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions.*
import kotlin.test.BeforeTest
import kotlin.test.Test

class AzureADServiceTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var azureADService: AzureADService

    @BeforeTest
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val mockServerBaseUrl = mockWebServer.url("/")

        azureADService =
            AzureADServiceImpl(
                httpClient = OkHttpClient(),
                tokenClient = MockOnBehalfOfClient,
                graphUrl = Url(mockServerBaseUrl.toString()),
            )
    }

    @Test
    fun `skal parse grupper fra respons`() =
        runBlocking {
            mockWebServer.enqueue(
                MockResponse()
                    .setBody(
                        """
                                        {
                            "@odata.context": "https://graph.microsoft.com/v1.0/${'$'}metadata#groups",
                            "@odata.count": 1,
                            "value": [
                                {
                                    "id": "d2987104-63b2-4110-83ac-20ff6afe24a2",
                                    "deletedDateTime": null,
                                    "classification": null,
                                    "createdDateTime": "2018-07-04T12:09:57Z",
                                    "creationOptions": [],
                                    "description": "Saksbehandler får regional tilgang til brukere i samme fylke som sin egen enhet.",
                                    "displayName": "0000-GA-GOSYS_REGIONAL",
                                    "expirationDateTime": null,
                                    "groupTypes": [],
                                    "isAssignableToRole": null,
                                    "mail": null,
                                    "mailEnabled": false,
                                    "mailNickname": "0000-GA-GOSYS_REGIONAL",
                                    "membershipRule": null,
                                    "membershipRuleProcessingState": null,
                                    "onPremisesDomainName": "preprod.local",
                                    "onPremisesLastSyncDateTime": "2023-06-26T07:07:06Z",
                                    "onPremisesNetBiosName": "PREPROD",
                                    "onPremisesSamAccountName": "0000-GA-GOSYS_REGIONAL",
                                    "onPremisesSecurityIdentifier": "S-1-5-21-303384491-3046432871-3340981675-29996",
                                    "onPremisesSyncEnabled": true,
                                    "preferredDataLocation": null,
                                    "preferredLanguage": null,
                                    "proxyAddresses": [],
                                    "renewedDateTime": "2018-07-04T12:09:57Z",
                                    "resourceBehaviorOptions": [],
                                    "resourceProvisioningOptions": [],
                                    "securityEnabled": true,
                                    "securityIdentifier": "S-1-12-1-3533205764-1091593138-4280331395-2720333418",
                                    "theme": null,
                                    "visibility": null,
                                    "onPremisesProvisioningErrors": []
                                }
                            ]
                        }
                        """.trimIndent(),
                    ).setResponseCode(200),
            )

            val result =
                azureADService.fetchRoller(
                    userToken = "fake-token",
                    veilederIdent = MockAzureADService.VEILEDER_NAV_IDENT,
                )
            assertEquals(1, result.size)

            val firstElement = result.first()

            assertEquals(MockAzureADService.GROUP_ID, firstElement.gruppeId)
            assertEquals(MockAzureADService.GROUP_NAME, firstElement.gruppeNavn)
        }

    @Test
    fun `skal ikke ha tilgang til gruppen hvis kallet mot MS Graph feiler`() =
        runBlocking {
            mockWebServer.enqueue(
                MockResponse()
                    .setBody(
                        """
                        {
                            "error": {
                                "code": "Request_BadRequest",
                                "message": "Invalid object identifier 'test'.",
                                "innerError": {
                                    "date": "2023-06-22T10:34:01",
                                    "request-id": "a13672c2-09a7-4632-8baf-02aa382260be",
                                    "client-request-id": "07874d41-b197-f868-41fe-f51a6879eb2d"
                                }
                            }
                        }
                        """.trimIndent(),
                    ).setResponseCode(400),
            )

            val result =
                azureADService.fetchRoller(
                    userToken = "fake-token",
                    veilederIdent = MockAzureADService.VEILEDER_NAV_IDENT,
                )
            assertEquals(0, result.size)
        }
}

private object MockOnBehalfOfClient : BoundedOnBehalfOfTokenClient {
    override fun exchangeOnBehalfOfToken(accesstoken: String): String = accesstoken
}
