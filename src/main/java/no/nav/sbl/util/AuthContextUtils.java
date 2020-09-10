package no.nav.sbl.util;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.SneakyThrows;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.utils.StringUtils;
import java.util.Optional;

public class AuthContextUtils {
    public static final String AAD_NAV_IDENT_CLAIM = "NAVident";
    public static String requireIdToken() {
        return AuthContextHolder.requireIdTokenString();
    }

    public static Optional<String> getIdent() {
        return AuthContextHolder.getIdTokenClaims()
                .map(AuthContextUtils::getSubject)
                .filter(StringUtils::notNullOrEmpty);
    }

    @SneakyThrows
    private static String getSubject(JWTClaimsSet claims) {
        String navIdent = claims.getStringClaim(AAD_NAV_IDENT_CLAIM);
        return navIdent != null ? navIdent : claims.getSubject();
    }
}
