package no.nav.sbl.service;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.SneakyThrows;
import no.nav.common.auth.context.AuthContext;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.client.msgraph.MsGraphClient;
import no.nav.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class AuthContextService {
    public static final String AAD_NAV_IDENT_CLAIM = "NAVident";
    private final MsGraphClient msGraphClient;

    @Autowired
    public AuthContextService(MsGraphClient msGraphClient) {
        this.msGraphClient = msGraphClient;
    }

    public Optional<String> getIdent() {
        return AuthContextHolder
                .getAccessToken()
                .map(msGraphClient::hentOnPremisesSamAccountName)
                .or(() -> AuthContextHolder.getIdTokenClaims().map(AuthContextService::getSubject))
                .filter(StringUtils::notNullOrEmpty);
    }

    @SneakyThrows
    private static String getSubject(JWTClaimsSet claims) {
        String navIdent = claims.getStringClaim(AAD_NAV_IDENT_CLAIM);
        return navIdent != null ? navIdent : claims.getSubject();
    }

    public static String requireIdToken() {
        return AuthContextHolder.requireIdTokenString();
    }
}
