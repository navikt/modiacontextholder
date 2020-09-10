package no.nav.sbl.service;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import no.nav.common.auth.context.AuthContext;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.UserRole;
import no.nav.common.client.msgraph.MsGraphClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static no.nav.sbl.service.AuthContextService.AAD_NAV_IDENT_CLAIM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthContextServiceTest {
    private static final String IDENT = "Z999999";
    private static final String AAD_V1_IDENT = "Z999990";
    private static final String AAD_V2_IDENT = "Z999991";
    private static final String AAD_ACCESS_TOKEN = "add-access-token-here";
    private static final AuthContext OPENAM_AUTH_CONTEXT = new AuthContext(
            UserRole.INTERN,
            new PlainJWT(new JWTClaimsSet.Builder().subject(IDENT).build())
    );
    private static final AuthContext AAD_V1_AUTH_CONTEXT = new AuthContext(
            UserRole.INTERN,
            new PlainJWT(new JWTClaimsSet.Builder().claim(AAD_NAV_IDENT_CLAIM, AAD_V1_IDENT).build())
    );
    private static final AuthContext AAD_V2_AUTH_CONTEXT = new AuthContext(
            UserRole.INTERN,
            new PlainJWT(new JWTClaimsSet.Builder().build()),
            AAD_ACCESS_TOKEN
    );

    @Mock
    MsGraphClient client;

    @InjectMocks
    AuthContextService authContextService;

    @Test
    public void skal_hente_ident_fra_openAm_token() {
        AuthContextHolder.withContext(OPENAM_AUTH_CONTEXT, () -> {
            assertThat(authContextService.getIdent()).hasValue(IDENT);
            verify(client, never()).hentOnPremisesSamAccountName(anyString());
            verify(client, never()).hentUserData(anyString());
        });
    }

    @Test
    public void skal_hente_ident_fra_aad_v1_token() {
        AuthContextHolder.withContext(AAD_V1_AUTH_CONTEXT, () -> {
            assertThat(authContextService.getIdent()).hasValue(AAD_V1_IDENT);
            verify(client, never()).hentOnPremisesSamAccountName(anyString());
            verify(client, never()).hentUserData(anyString());
        });
    }

    @Test
    public void skal_hente_ident_fra_graph_api_om_accesstoken_eksisterer() {
        when(client.hentOnPremisesSamAccountName(eq(AAD_ACCESS_TOKEN))).thenReturn(AAD_V2_IDENT);
        AuthContextHolder.withContext(AAD_V2_AUTH_CONTEXT, () -> {
            assertThat(authContextService.getIdent()).hasValue(AAD_V2_IDENT);
            verify(client, times(1)).hentOnPremisesSamAccountName(anyString());
            verify(client, never()).hentUserData(anyString());
        });
    }
}
