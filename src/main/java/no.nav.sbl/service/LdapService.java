package no.nav.sbl.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.sbl.util.EnvironmentUtils;
import org.springframework.stereotype.Service;

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.*;

import static java.util.Optional.ofNullable;

//må bruke Hashtable i InitiallLdapContext dessverre.
@SuppressWarnings({"squid:S1149"})
@Slf4j
@Service
public class LdapService {
    public static final String LDAP_USERNAME = "LDAP_USERNAME";
    public static final String LDAP_PASSWORD = "LDAP_PASSWORD";

    private static Hashtable<String, String> env = new Hashtable<>();
    private static String SEARCHBASE;

    public LdapService() {
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, EnvironmentUtils.getRequiredProperty("ldap.url", "LDAP_URL"));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, EnvironmentUtils.getRequiredProperty("ldap.username", LDAP_USERNAME));
        env.put(Context.SECURITY_CREDENTIALS, EnvironmentUtils.getRequiredProperty("ldap.password", LDAP_PASSWORD));

        SEARCHBASE = "OU=Users,OU=NAV,OU=BusinessUnits," + EnvironmentUtils.getRequiredProperty("ldap.basedn", "LDAP_BASEDN");
    }

    public Map hentVeilederAttributter(String veilederUid, List<String> attributter) {
        Map map = new HashMap<>();

        try {
            SearchControls searchCtrl = new SearchControls();
            searchCtrl.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> result = ldapContext().search(SEARCHBASE, String.format("(&(objectClass=user)(CN=%s))", veilederUid), searchCtrl);
            if (result.hasMore()) {
                Attributes ldapAttributes = result.next().getAttributes();
                populateAttributtMap(attributter, map, ldapAttributes);
            } else {
                throw new RuntimeException("Fant ingen attributter i resultat fra ldap for veileder " + veilederUid);
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    private void populateAttributtMap(List<String> attributter, Map map, Attributes ldapAttributes) {
        attributter.forEach(s -> {
            try {
                map.put(s, ofNullable(ldapAttributes.get(s).get()).orElseThrow(() -> new RuntimeException("Fant ikke attributt " + s)));
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static LdapContext ldapContext() throws NamingException {
        return new InitialLdapContext(env, null);
    }
}
