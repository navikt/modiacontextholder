package no.nav.modiacontextholder.rest

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class NaisRoutesTest : TestApplication() {
    @Test
    internal fun `is ready`() =
        testApp {
            var retryCount = 0
            while (retryCount < 5) {
                val res =
                    client.get("/internal/isReady")
                if (res.status == HttpStatusCode.OK) {
                    assertEquals(res.status, HttpStatusCode.OK)
                    break
                }
                delay(500)
                retryCount++
                continue
            }
        }

    @Test
    internal fun `is alive`() =
        testApp {
            var retryCount = 0
            while (retryCount < 5) {
                val res =
                    client.get("/internal/isAlive")
                if (res.status == HttpStatusCode.OK) {
                    assertEquals(res.status, HttpStatusCode.OK)
                    break
                }
                delay(500)
                retryCount++
                continue
            }
        }
}
