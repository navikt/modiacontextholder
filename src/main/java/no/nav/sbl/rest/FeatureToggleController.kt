package no.nav.sbl.rest

import no.nav.sbl.service.unleash.UnleashService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

private const val APPLICATION_PREFIX = "modiacontextholder."

@RestController
@RequestMapping("/rest/featuretoggle")
class FeatureToggleController @Autowired constructor(
    private val unleashService: UnleashService,
) {

    @GetMapping("/{id}")
    fun hentMedId(@PathVariable("id") toggleId: String): Boolean =
        unleashService.isEnabled(sjekkPrefix(toggleId))

    @GetMapping
    fun hentToggles(@RequestParam(value = "id", required = false) ids: Set<String>?): Map<String, Boolean> {
        return (ids ?: emptySet()).associateWith {
                    unleashService.isEnabled(sjekkPrefix(it))
                }
    }

    private fun sjekkPrefix(propertyKey: String): String {
        return if (propertyKey.contains(".")) propertyKey else APPLICATION_PREFIX + propertyKey
    }
}
