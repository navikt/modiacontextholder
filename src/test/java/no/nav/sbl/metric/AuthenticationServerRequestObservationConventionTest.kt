package no.nav.sbl.metric

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.config.AuthenticationServerRequestObservationConvention
import no.nav.sbl.service.AuthContextService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.http.server.observation.ServerRequestObservationConvention
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Optional

@SpringBootTest(
    classes = [AuthenticationServerRequestObservationConventionTestConfiguration::class],
    properties = [
        "management.endpoint.metrics.enabled=true",
        "management.endpoint.prometheus.enabled=true",
        "management.endpoints.web.base-path=/internal",
        "management.endpoints.web.exposure.include=prometheus",
        "management.prometheus.metrics.export.enabled=true"
    ]
    )
@AutoConfigureMockMvc
@EnableAutoConfiguration
class AuthenticationServerRequestObservationConventionTest {
    @Autowired
    lateinit var authContextService: AuthContextService

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `metrics are collected with unknown authentication type`() {
        every { authContextService.authorizedPartyName } returns Optional.empty()

        mockMvc.get("/test")
            .andExpect {
                status { isOk() }
            }

        val result = mockMvc.get("/internal/prometheus")
            .andExpect {
                status { isOk() }
            }
            .andReturn().response.contentAsString

        // Verify that the custom metric with the authentication_type tag is present
        assert(result.contains("""http_server_requests_seconds_count{authorized_party="unknown",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/test"} 1"""))
    }

    @Test
    fun `metrics are collected with authentication type`() {
        every { authContextService.authorizedPartyName } returns Optional.of("dev-gcp:aura:nais-testapp")

        mockMvc.get("/test")
            .andExpect {
                status { isOk() }
            }

        val result = mockMvc.get("/internal/prometheus")
            .andExpect {
                status { isOk() }
            }
            .andReturn().response.contentAsString

        // Verify that the custom metric with the authentication_type tag is present
        assert(result.contains("""http_server_requests_seconds_count{authorized_party="dev-gcp:aura:nais-testapp",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/test"} 1"""))
    }
}

@Configuration
open class AuthenticationServerRequestObservationConventionTestConfiguration {
    @Bean
    open fun mockAuthContextService(): AuthContextService = mockk()

    @Bean
    open fun authenticationServerRequestObservationConvention(authContextService: AuthContextService): ServerRequestObservationConvention =
        AuthenticationServerRequestObservationConvention(authContextService)

    @RestController
    open class TestController {
        @GetMapping("/test")
        fun test(): ResponseEntity<Unit> {
            return ResponseEntity.ok().build()
        }
    }
}