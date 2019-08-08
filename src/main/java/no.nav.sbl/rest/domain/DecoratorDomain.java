package no.nav.sbl.rest.domain;

import java.util.List;

public class DecoratorDomain {
    public static class DecoratorConfig {
        public final String ident, navn, fornavn, etternavn;
        public final List<Enhet> enheter;

        public DecoratorConfig(Saksbehandler saksbehandler, List<Enhet> enheter) {
            this.ident = saksbehandler.ident;
            this.fornavn = saksbehandler.fornavn;
            this.etternavn = saksbehandler.etternavn;
            this.navn = saksbehandler.navn;
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

    public static class Saksbehandler {
        public final String ident, navn, fornavn, etternavn;

        public Saksbehandler(String ident, String fornavn, String etternavn) {
            this.ident = ident;
            this.fornavn = fornavn;
            this.etternavn = etternavn;
            this.navn = fornavn + " " + etternavn;
        }
    }
}
