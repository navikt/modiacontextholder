package no.nav.sbl.service;

import no.nav.sbl.rest.domain.DecoratorDomain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import java.util.Map;

import static java.util.Arrays.asList;

public class VeilederService {

    @Autowired
    LdapService ldapService;


    @Cacheable("veilederCache")
    public DecoratorDomain.Saksbehandler hentVeilederNavn(String ident) {
        Map map = ldapService.hentVeilederAttributter(ident, asList("givenname", "sn"));

        return new DecoratorDomain.Saksbehandler(
                ident,
                map.get("givenname").toString(),
                map.get("sn").toString()
        );
    }
}
