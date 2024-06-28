package no.nav.sbl.rest

import org.junit.Before
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.mock.web.MockServletContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(
    ContextPathRedirect::class,
)
@TestPropertySource(locations = ["classpath:application.properties"])
class ContextPathRedirectTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Before
    fun setUp() {
        (mockMvc.dispatcherServlet.servletContext as MockServletContext).contextPath = "/modiacontextholder"
    }

    @Test
    fun `Redirects requests still using the context path correctly`() {
        mockMvc.get("/modiacontextholder/test/path").andExpect {
            redirectedUrl("/test/path")
        }
    }

    @Test
    fun `Works with context path`() {
        mockMvc.get("/modiacontextholder/modiacontextholder/test/path") { contextPath = "/modiacontextholder" }.andExpect {
            redirectedUrl("/modiacontextholder/test/path")
        }
    }

    @Test
    fun `Does not redirect requests on the context path`() {
        mockMvc.get("/modiacontextholder/test/path") { contextPath = "/modiacontextholder" }.andExpect {
            status { isNotFound() }
        }
    }
}
