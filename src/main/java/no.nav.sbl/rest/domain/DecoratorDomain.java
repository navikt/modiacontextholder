package no.nav.sbl.rest.domain;

import java.util.List;

public class DecoratorDomain {
    public static class DecoratorConfig {
        public final String ident, navn, fornavn, etternavn;
        public final List<Enhet> enheter;

        public DecoratorConfig(String ident, String fornavn, String etternavn, List<Enhet> enheter) {
            this.ident = ident;
            this.fornavn = fornavn;
            this.etternavn = etternavn;
            this.navn = fornavn + " " + etternavn;
            this.enheter = enheter;
        }
    }

    public static class Enhet {
        public final String enhetId, navn;

        public Enhet(String enhetId, String navn) {
            this.enhetId = enhetId;
            this.navn = navn;
        }
    }
}
