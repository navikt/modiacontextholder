package no.nav.modiacontextholder.routes

import io.getunleash.FakeUnleash
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.modiacontextholder.rest.TestApplication
import org.assertj.core.api.Assertions.*
import org.koin.test.inject
import kotlin.test.Test
import kotlin.test.assertEquals

class FeatureToggleRoutesTest : TestApplication() {
    @Test
    fun testGetApiFeaturetoggle() =
        testApp {
            val fakeUnleash: FakeUnleash by inject()
            fakeUnleash.enable("modiacontextholder.featureone")

            client.get("/api/featuretoggle?id=featureone,modiacontextholder.featureone,featuretwo").apply {
                assertEquals(HttpStatusCode.OK, this.status)
                assertThat(
                    this.bodyAsText(),
                ).isEqualToIgnoringWhitespace(
                    """{
                        "featureone": true,
                        "modiacontextholder.featureone": true,
                        "featuretwo": false
                    }
                    """,
                )
            }
        }

    @Test
    fun testGetApiFeaturetoggleId() =
        testApp {
            val fakeUnleash: FakeUnleash by inject()
            fakeUnleash.enable("modiacontextholder.featureone")

            client.get("/api/featuretoggle/featureone").apply {
                assertEquals(HttpStatusCode.OK, this.status)
                assertEquals("true", this.bodyAsText())
            }
        }
}
