package no.nav.modiacontextholder.consumers.norg2.domain

import java.io.Serializable

data class Enhet(
    var enhetNr: String,
    var navn: String,
    var status: String,
) : Serializable
