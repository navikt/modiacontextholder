package no.nav.sbl.service;

import no.nav.common.client.nom.NomClient;
import no.nav.common.client.nom.VeilederNavn;
import no.nav.common.types.identer.NavIdent;
import no.nav.sbl.rest.domain.DecoratorDomain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

public class VeilederService {

    @Autowired
    NomClient nomClient;

    @Cacheable("veilederCache")
    public DecoratorDomain.Saksbehandler hentVeilederNavn(String ident) {
        VeilederNavn veilederNavn = nomClient.finnNavn(new NavIdent(ident));

        return new DecoratorDomain.Saksbehandler(
                ident,
                veilederNavn.getFornavn(),
                veilederNavn.getEtternavn()
        );
    }
}
