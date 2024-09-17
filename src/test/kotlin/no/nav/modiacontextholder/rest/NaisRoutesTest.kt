package no.nav.modiacontextholder.rest

import io.ktor.client.request.*
import io.ktor.http.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class NaisRoutesTest : TestApplication() {
    @Test
    internal fun `is ready`() =
        testApp {
            val res =
                it
                    .get("/internal/isAlive")

            assertEquals(res.status, HttpStatusCode.OK)
        }

    @Test
    internal fun `is alive`() =
        testApp {
            val res = client.get("/internal/isAlive")
            assertEquals(res.status, HttpStatusCode.OK)
        }
}
