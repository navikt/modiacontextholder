package no.nav.sbl.rest.domain

import lombok.Data
import lombok.EqualsAndHashCode
import lombok.experimental.Accessors

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
data class RSAktivBruker(
    val aktivBruker: String?,
)
