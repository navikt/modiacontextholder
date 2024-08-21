package no.nav.sbl.rest

import no.nav.common.health.HealthCheck
import no.nav.common.health.selftest.SelfTestUtils
import no.nav.common.health.selftest.SelftestHtmlGenerator
import no.nav.sbl.config.Pingable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal")
class NaisController(
    private val healthChecks: List<HealthCheck>,
    private val pingables: List<Pingable>,
) {
    @GetMapping("/isReady")
    fun isReady(): ResponseEntity<Void> = ResponseEntity.status(200).build()

    @GetMapping("/isAlive")
    fun isAlive(): ResponseEntity<Void> {
        val anyIsUnhealthy = healthChecks.map { it.checkHealth() }.any { it.isUnhealthy }
        return if (anyIsUnhealthy) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        } else {
            ResponseEntity.status(HttpStatus.OK).build()
        }
    }

    @GetMapping("/selftest")
    fun selftest(): ResponseEntity<String> {
        val result = SelfTestUtils.checkAll(pingables.map { it.ping() })
        return ResponseEntity
            .status(SelfTestUtils.findHttpStatusCode(result))
            .contentType(MediaType.TEXT_HTML)
            .body(SelftestHtmlGenerator.generate(result))
    }
}
