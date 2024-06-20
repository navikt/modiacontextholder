package no.nav.sbl.config

import no.nav.common.utils.EnvironmentUtils

object ApplicationCluster {
    fun isGcp(): Boolean =
        when (EnvironmentUtils.getRequiredProperty("NAIS_CLUSTER_NAME")) {
            "dev-gcp", "prod-gcp" -> true
            else -> false
        }

    fun isFss(): Boolean =
        when (EnvironmentUtils.getRequiredProperty("NAIS_CLUSTER_NAME")) {
            "dev-fss", "prod-fss" -> true
            else -> false
        }
}
