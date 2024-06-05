package no.nav.sbl.service;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.SneakyThrows;
import no.nav.common.client.msgraph.MsGraphClient;
import no.nav.common.utils.StringUtils;
import no.nav.sbl.util.AuthContextUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.Optional;

public class AuthContextService {
    public static final String AAD_NAV_IDENT_CLAIM = "NAVident";
    private final MsGraphClient msGraphClient;

    @Autowired
    public AuthContextService(MsGraphClient msGraphClient) {
        this.msGraphClient = msGraphClient;
    }

    public Optional<String> getIdent() {
        return AuthContextUtils
                .getAccessToken()
                .map(msGraphClient::hentOnPremisesSamAccountName)
                .or(() -> AuthContextUtils.getIdTokenClaims().map(AuthContextService::getSubject))
                .filter(StringUtils::notNullOrEmpty);
    }

    public Optional<String> getAuthorizedPartyName() {
        return AuthContextUtils.getIdTokenClaims()
                .flatMap(AuthContextService::getAuthorizedParty);
    }

    private static Optional<String> getAuthorizedParty(JWTClaimsSet claims) {
        try {
            return Optional.ofNullable(claims.getStringClaim("azp_name"));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    @SneakyThrows
    private static String getSubject(JWTClaimsSet claims) {
        String navIdent = claims.getStringClaim(AAD_NAV_IDENT_CLAIM);
        return navIdent != null ? navIdent : claims.getSubject();
    }

    public String requireIdToken() {
        return AuthContextUtils.requireIdTokenString();
    }
}
