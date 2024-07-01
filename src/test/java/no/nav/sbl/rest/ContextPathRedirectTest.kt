package no.nav.sbl.metric

import io.mockk.mockk
import no.nav.sbl.service.AuthContextService
import no.nav.sbl.util.RewriteContextPathFilter
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootTest(
    classes = [ContextPathRedirectTestConfiguration::class],
)
@AutoConfigureMockMvc
class ContextPathRedirectTest{
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `Redirects requests still using the context path`() {
        mockMvc.get("/modiacontextholder/test").andExpect {
            status { isOk() }
            forwardedUrl("/test")
        }
    }

    @Test
    fun `Works with context path`() {
       mockMvc.get("/modiacontextholder/modiacontextholder/test") { contextPath = "/modiacontextholder" }.andExpect {
           status { isOk() }
           forwardedUrl("/modiacontextholder/test")
       }
    }

    @Test
    fun `Does not redirect requests on the context path`() {
       val res = mockMvc.get("/modiacontextholder/test") { contextPath = "/modiacontextholder" }.andExpect {
            status { isOk() }
        }.andReturn().response.contentAsString
        assert(res.equals("test"))
    }


}

@Configuration
open class ContextPathRedirectTestConfiguration {
    @Bean
    open fun authContextService(): AuthContextService = AuthContextService(mockk())

    @Bean
    open fun redirectContextPathFilter(
    ): FilterRegistrationBean<RewriteContextPathFilter> =
        FilterRegistrationBean(
            RewriteContextPathFilter()
        ).apply {
            urlPatterns = listOf("*")
            order = -1
        }

    @RestController
    open class TestController {
        @GetMapping("/test", produces = ["text/plain"])
        fun test(): String {
            return "test"
        }
    }
}
