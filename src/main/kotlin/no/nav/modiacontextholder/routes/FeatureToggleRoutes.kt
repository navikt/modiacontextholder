package no.nav.modiacontextholder.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.modiacontextholder.service.unleash.ToggleableFeatureService
import org.koin.ktor.ext.inject

private const val APPLICATION_PREFIX = "modiacontextholder."

fun Route.featureToggleRoutes() {
    val featureToggleService: ToggleableFeatureService by inject()

    route("/api/featuretoggle") {
        /**
         * @OpenAPITag featuretoggle
         */
        get("/{id}") {
            val feature = call.parameters["id"]

            if (feature.isNullOrEmpty()) {
                call.respondText("false")
            } else {
                call.respondText(featureToggleService.isEnabled(checkPrefix(feature)).toString())
            }
        }

        /**
         * @OpenAPITag featuretoggle
         */
        get("") {
            val ids =
                call.request.queryParameters["id"]
                    ?.split(",")
                    ?.toSet() ?: emptySet()

            val response = ids.associateWith { featureToggleService.isEnabled(checkPrefix(it)) }
            call.respond(response)
        }
    }
}

fun checkPrefix(propertyKey: String): String = if (propertyKey.contains(".")) propertyKey else APPLICATION_PREFIX + propertyKey
