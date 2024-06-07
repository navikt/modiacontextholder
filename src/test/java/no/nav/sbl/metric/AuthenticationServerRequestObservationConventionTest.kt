package no.nav.sbl.metric

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.PlainJWT
import io.micrometer.observation.ObservationRegistry
import io.mockk.mockk
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import no.nav.common.auth.context.AuthContext
import no.nav.common.auth.context.AuthContextHolderThreadLocal
import no.nav.common.auth.context.UserRole
import no.nav.sbl.config.CustomServerRequestObservationConvention
import no.nav.sbl.service.AuthContextService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.http.server.observation.ServerRequestObservationConvention
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.filter.ServerHttpObservationFilter
import java.util.Date

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
    companion object {
        /**
         * Delegert til [AuthenticationServerRequestObservationConventionTestConfiguration.authFilter].
         * Oppdater den for å sette hvilke claims som skal brukes i testen som kjøres.
         */
        lateinit var claimsSet: JWTClaimsSet
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `metrics are collected with authorized party name`() {
        claimsSet = generateClaimSet()

        mockMvc.get("/test").andExpect {
            status { isOk() }
        }

        val result = mockMvc.get("/internal/prometheus").andExpect {
            status { isOk() }
        }.andReturn().response.contentAsString

        // Verify that the custom metric with the authentication_type tag is present
        assert(result.contains("""http_server_requests_seconds_count{authorized_party="unknown",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/test"} 1"""))
    }

    @Test
    fun `metrics are collected with authentication type`() {
        claimsSet = generateClaimSet("azp_name" to "dev-gcp:aura:nais-testapp")

        mockMvc.get("/test").andExpect {
            status { isOk() }
        }

        val result = mockMvc.get("/internal/prometheus").andExpect {
            status { isOk() }
        }.andReturn().response.contentAsString

        // Verify that the custom metric with the authentication_type tag is present
        assert(result.contains("""http_server_requests_seconds_count{authorized_party="dev-gcp:aura:nais-testapp",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/test"} 1"""))
    }

    private fun generateClaimSet(vararg claims: Pair<String, String>): JWTClaimsSet =
        JWTClaimsSet.Builder().apply {
            subject("user123")
            issuer("your-issuer")
            expirationTime(Date(System.currentTimeMillis() + 3600 * 1000))
            claims.forEach { claim(it.first, it.second) }
        }.build()
}

@Configuration
open class AuthenticationServerRequestObservationConventionTestConfiguration {
    @Bean
    open fun authContextService(): AuthContextService = AuthContextService(mockk())

    @Bean
    open fun authenticationServerRequestObservationConvention(authContextService: AuthContextService): ServerRequestObservationConvention =
        CustomServerRequestObservationConvention(authContextService)

    @Bean("authFilter")
    open fun authFilter(): Filter = object : Filter {
        override fun doFilter(
            request: ServletRequest,
            response: ServletResponse,
            chain: FilterChain,
        ) {
            val claimsSet by AuthenticationServerRequestObservationConventionTest::claimsSet

            val plainJWT = PlainJWT(claimsSet)
            val authContext = AuthContext(UserRole.INTERN, plainJWT)

            AuthContextHolderThreadLocal.instance().withContext(authContext) {
                chain.doFilter(request, response)
            }
        }
    }

    @Bean
    open fun accessTokenFilter(
        @Qualifier("authFilter") authFilter: Filter,
    ): FilterRegistrationBean<Filter> = FilterRegistrationBean(authFilter).apply {
        urlPatterns = listOf("/*")
        order = 1
    }

    @Bean
    open fun serverHttpObservationFilter(
        observationRegistry: ObservationRegistry,
        serverRequestObservationConvention: ServerRequestObservationConvention
    ): FilterRegistrationBean<ServerHttpObservationFilter> =
        FilterRegistrationBean(
            ServerHttpObservationFilter(observationRegistry, serverRequestObservationConvention)
        ).apply {
            urlPatterns = listOf("/*")
            order = 2
        }

    @RestController
    open class TestController {
        @GetMapping("/test")
        fun test(): ResponseEntity<Unit> {
            return ResponseEntity.ok().build()
        }
    }
}