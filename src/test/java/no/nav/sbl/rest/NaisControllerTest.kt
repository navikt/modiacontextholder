package no.nav.sbl.rest

import io.mockk.mockk
import no.nav.sbl.config.Pingable
import no.nav.sbl.util.WebMvcTestUtils.getJson
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@WebMvcTest(NaisController::class)
internal class NaisControllerTest {

    @TestConfiguration
    open class TestConfig {
        @Bean
        open fun pingables() = mockk<Pingable>()
    }

    @Autowired
    lateinit var mockMvc: MockMvc


    @Test
    internal fun `is ready`() {
        mockMvc.getJson("/internal/isReady")
            .andExpect {
                assertThat(it.response.status).isEqualTo(200)
            }
    }

    @Test
    internal fun `is alive`() {
        mockMvc.getJson("/internal/isAlive")
            .andExpect {
                assertThat(it.response.status).isEqualTo(200)
            }
    }
}
