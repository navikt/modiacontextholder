package no.nav.modiacontextholder

import no.nav.personoversikt.common.utils.EnvUtils

const val APP_NAME = "modiacontextholder"

val appImage = EnvUtils.getConfig("NAIS_APP_IMAGE") ?: "N/A"
