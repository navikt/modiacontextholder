package no.nav.sbl.rest

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(ContextPathRedirect::class)
class ContextPathRedirectTest  {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `Redirects requests still using the context path correctly`() {
        mockMvc.get("/modiacontextholder/test/path").andExpect {
            redirectedUrl("/test/path")
        }
    }
}