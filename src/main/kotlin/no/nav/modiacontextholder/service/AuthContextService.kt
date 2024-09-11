package no.nav.modiacontextholder.service

import com.nimbusds.jwt.JWTClaimsSet
import no.nav.common.client.msgraph.MsGraphClient
import no.nav.common.utils.StringUtils
import no.nav.modiacontextholder.util.AuthContextUtils
import java.text.ParseException
import java.util.*
import kotlin.jvm.optionals.getOrNull

class AuthContextService(
    private val msGraphClient: MsGraphClient,
) {
    companion object {
        const val AAD_NAV_IDENT_CLAIM = "NAVident"
    }

    val ident: Optional<String>
        get() =
            AuthContextUtils
                .getAccessToken()
                .map { msGraphClient.hentOnPremisesSamAccountName(it) }
                .or { AuthContextUtils.getIdTokenClaims().map { claims -> getSubject(claims) } }
                .filter { StringUtils.notNullOrEmpty(it) }

    fun getAuthorizedPartyName(): Optional<String> =
        AuthContextUtils
            .getIdTokenClaims()
            .flatMap { claims -> getAuthorizedParty(claims) }

    fun getAccessToken(): String? = AuthContextUtils.getAccessToken().getOrNull()

    private fun getAuthorizedParty(claims: JWTClaimsSet): Optional<String> =
        try {
            Optional.ofNullable(claims.getStringClaim("azp_name"))
        } catch (_: ParseException) {
            Optional.empty()
        }

    private fun getSubject(claims: JWTClaimsSet): String {
        val navIdent = claims.getStringClaim(AAD_NAV_IDENT_CLAIM)
        return navIdent ?: claims.subject
    }

    fun requireIdToken(): String = AuthContextUtils.requireIdTokenString()
}
