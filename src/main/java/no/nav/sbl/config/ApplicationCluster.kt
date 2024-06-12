package no.nav.sbl.config

enum class ApplicationCluster {
    LOCAL, DEV_FSS, PROD_FSS, DEV_GCP, PROD_GCP;

    fun isGcp(): Boolean {
        return when (this) {
            DEV_GCP, PROD_GCP -> true
            else -> false
        }
    }

    companion object {
        fun fromClusterName(clusterName: String): ApplicationCluster {
            return when (clusterName) {
                "dev-fss" -> DEV_FSS
                "prod-fss" -> PROD_FSS
                "dev-gcp" -> DEV_GCP
                "prod-gcp" -> PROD_GCP
                else -> LOCAL
            }
        }
    }
}