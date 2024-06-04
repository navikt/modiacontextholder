package no.nav.sbl.metric

import no.nav.sbl.config.AuthenticationServerRequestObservationConvention
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.http.server.observation.ServerRequestObservationConvention
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Configuration
open class AuthenticationServerRequestObservationConventionTestConfiguration {
    @Bean
    open fun authenticationServerRequestObservationConvention(): ServerRequestObservationConvention =
        AuthenticationServerRequestObservationConvention()

    @RestController
    open class TestController {
        @GetMapping("/test")
        fun test(): ResponseEntity<Unit> {
            return ResponseEntity.ok().build()
        }
    }
}