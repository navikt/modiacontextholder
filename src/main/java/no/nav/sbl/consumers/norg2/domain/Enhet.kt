package no.nav.sbl.consumers.norg2.domain

import java.io.Serializable

data class Enhet(
    var enhetNr: String? = null,
    var navn: String? = null,
    var status: String? = null
) : Serializable