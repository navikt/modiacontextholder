package no.nav.sbl.config

import no.nav.sbl.config.ApplicationCluster.Cluster.DEV_FSS
import no.nav.sbl.config.ApplicationCluster.Cluster.DEV_GCP
import no.nav.sbl.config.ApplicationCluster.Cluster.LOCAL
import no.nav.sbl.config.ApplicationCluster.Cluster.PROD_FSS
import no.nav.sbl.config.ApplicationCluster.Cluster.PROD_GCP

open class ApplicationCluster(
    clusterName: String,
) {
    open fun isGcp(): Boolean =
        when (cluster) {
            DEV_GCP, PROD_GCP -> true
            else -> false
        }

    open fun isFss(): Boolean =
        when (cluster) {
            DEV_FSS, PROD_FSS -> true
            else -> false
        }

    private val cluster =
        when (clusterName) {
            "dev-fss" -> DEV_FSS
            "prod-fss" -> PROD_FSS
            "dev-gcp" -> DEV_GCP
            "prod-gcp" -> PROD_GCP
            else -> LOCAL
        }

    private enum class Cluster {
        LOCAL,
        DEV_FSS,
        PROD_FSS,
        DEV_GCP,
        PROD_GCP,
    }
}
