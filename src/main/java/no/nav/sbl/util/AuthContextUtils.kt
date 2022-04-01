package no.nav.sbl.util

import com.nimbusds.jwt.JWTClaimsSet
import no.nav.common.auth.context.AuthContext
import no.nav.common.auth.context.AuthContextHolderThreadLocal
import no.nav.common.utils.fn.UnsafeRunnable
import no.nav.sbl.util.AccesstokenServletFilter.AccesstokenHolderThreadLocal
import java.util.*

object AuthContextUtils {
    private val authContextHolder = AuthContextHolderThreadLocal.instance()
    private val accesstokenHolder = AccesstokenHolderThreadLocal.instance()

    @JvmStatic
    fun getAccessToken(): Optional<String> = accesstokenHolder.getAccesstoken()

    @JvmStatic
    fun getIdTokenClaims(): Optional<JWTClaimsSet> = authContextHolder.idTokenClaims

    @JvmStatic
    fun requireIdTokenString(): String = authContextHolder.requireIdTokenString()

    @JvmStatic
    fun withContext(authContext: AuthContext?, block: UnsafeRunnable) = authContextHolder.withContext(authContext, block)

    @JvmStatic
    fun withAccesstoken(accesstoken: String?, block: UnsafeRunnable) = accesstokenHolder.withAccesstoken(accesstoken, block)
}