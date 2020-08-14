import no.nav.sbl.log
import io.ktor.auth.Authentication


fun Authemvnntication.Configuration.setupMock(mockPrincipal: SubjectPrincipal) {
    mock {
        principal = mockPrincipal
    }
}

fun Authentication.Configuration.setupJWT(jwksUrl: String) {
    jwt {
        authHeader(Security::useJwtFromCookie)
        verifier(Security.makeJwkProvider(jwksUrl))
        realm = "no.nav.sbl.modiacontextholder"
        validate { Security.validateJWT(it) }
    }
}
object Security {
    private const val cookieName = "ID_token"

    fun getSubject(call: ApplicationCall): String {
        return try {
            useJwtFromCookie(call)
                    ?.getBlob()
                    ?.let { blob -> JWT.decode(blob).parsePayload().subject }
                    ?: "Unauthenticated"
        } catch (e: Throwable) {
            "Invalid JWT"
        }
    }

    internal fun useJwtFromCookie(call: ApplicationCall): HttpAuthHeader? {
        return try {
            val token = call.request.cookies[cookieName]
            io.ktor.http.auth.parseAuthorizationHeader("Bearer $token")
        } catch (ex: Throwable) {
            log.warn("Could not get JWT from cookie '$cookieName'", ex)
            null
        }
    }

    internal fun makeJwkProvider(jwksUrl: String): JwkProvider =
            JwkProviderBuilder(URL(jwksUrl))
                    .cached(10, 24, TimeUnit.HOURS)
                    .rateLimited(10, 1, TimeUnit.MINUTES)
                    .build()

    internal fun validateJWT(credentials: JWTCredential): Principal? {
        return try {
            requireNotNull(credentials.payload.audience) { "Audience not present" }
            SubjectPrincipal(credentials.payload.subject)
        } catch (e: Exception) {
            log.error("Failed to validateJWT token", e)
            null
        }
    }

    private fun HttpAuthHeader.getBlob() = when {
        this is HttpAuthHeader.Single -> blob
        else -> null
    }

    private fun DecodedJWT.parsePayload(): Payload {
        val payloadString = String(Base64.getUrlDecoder().decode(payload))
        return JWTParser().parsePayload(payloadString)
    }
}

class SubjectPrincipal(val subject: String) : Principal
