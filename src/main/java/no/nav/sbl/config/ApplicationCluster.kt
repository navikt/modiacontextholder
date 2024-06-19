package no.nav.sbl.config

open class ApplicationCluster(
    clusterName: String,
) {
    private val cluster = Cluster.fromClusterName(clusterName)

    fun isGcp(): Boolean = cluster.isGcp()

    fun isFss(): Boolean = cluster.isFss()

    enum class Cluster {
        LOCAL,
        DEV_FSS,
        PROD_FSS,
        DEV_GCP,
        PROD_GCP,
        ;

        companion object {
            fun fromClusterName(clusterName: String): Cluster =
                when (clusterName) {
                    "dev-fss" -> DEV_FSS
                    "prod-fss" -> PROD_FSS
                    "dev-gcp" -> DEV_GCP
                    "prod-gcp" -> PROD_GCP
                    else -> LOCAL
                }
        }

        fun isGcp(): Boolean =
            when (this) {
                DEV_GCP, PROD_GCP -> true
                else -> false
            }

        fun isFss(): Boolean =
            when (this) {
                DEV_FSS, PROD_FSS -> true
                else -> false
            }
    }
}
