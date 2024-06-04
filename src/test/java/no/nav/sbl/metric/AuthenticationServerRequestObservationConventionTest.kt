package no.nav.sbl.metric

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

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
    lateinit var mockMvc: MockMvc

    @Test
    fun `metrics are collected with authentication type`() {
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
        assert(result.contains("""authentication_server_request_observation_convention_seconds_count{authentication_type="unknown",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/test"} 1"""))
    }
}