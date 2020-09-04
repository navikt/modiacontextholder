package no.nav.sbl.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.common.utils.fn.UnsafeConsumer;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable("ldapCache")
    public Map<String, Object> hentVeilederAttributter(String veilederUid, List<String> attributter) {
        Map<String, Object> map = new HashMap<>();
        NamingEnumeration<SearchResult> result = sokLDAP(veilederUid);

        forEach(result, "Fant ingen attributter i resultat fra ldap for veileder " + veilederUid, (searchResult) -> {
            populateAttributtMap(attributter, map, searchResult.getAttributes());
        });

        return map;
    }

    @Cacheable("veilederRolleCache")
    public List<String> hentVeilederRoller(String veilederUid) {
        NamingEnumeration<SearchResult> result = sokLDAP(veilederUid);
        List<String> roller = new ArrayList<>();

        forEach(result, "Fant ingen attributter i resultat fra ldap for veileder " + veilederUid, (searchResult) -> {
            NamingEnumeration<?> memberof = searchResult.getAttributes().get("memberof").getAll();
            forEach(memberof, "", (attributes) -> parseAttributtTilRolle(attributes).ifPresent(roller::add));
        });

        return roller;
    }

    private Optional<String> parseAttributtTilRolle(Object attributes) {
        if (attributes instanceof String) {
            String attr = (String) attributes;
            if (!attr.startsWith("CN=")) {
                return Optional.empty();
            }
            return Optional.ofNullable(attr.split(",")[0].split("CN=")[1]);
        }
        return Optional.empty();
    }

    private NamingEnumeration<SearchResult> sokLDAP(String veilederUid) {
        SearchControls searchCtrl = new SearchControls();
        searchCtrl.setSearchScope(SearchControls.SUBTREE_SCOPE);

        try {
            return ldapContext().search(SEARCHBASE, String.format("(&(objectClass=user)(CN=%s))", veilederUid), searchCtrl);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    private void populateAttributtMap(List<String> attributter, Map<String, Object> map, Attributes ldapAttributes) {
        attributter.forEach((attributt) -> {
            try {
                map.put(attributt, ofNullable(ldapAttributes.get(attributt).get()).orElseThrow(() -> new RuntimeException("Fant ikke attributt " + attributt)));
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static LdapContext ldapContext() throws NamingException {
        return new InitialLdapContext(env, null);
    }

    private static <T> void forEach(NamingEnumeration<T> data, String ifEmptyError, UnsafeConsumer<T> fn) {
        try {
            if (!data.hasMore()) {
                throw new RuntimeException(ifEmptyError);
            }
            while (data.hasMore()) {
                fn.accept(data.next());
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
}
