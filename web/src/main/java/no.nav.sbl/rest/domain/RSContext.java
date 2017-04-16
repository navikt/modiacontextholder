package no.nav.sbl.rest.domain;

public class RSContext {

    public transient String ip;
    public String aktivBruker;
    public String aktivEnhet;

    public RSContext withAktivBruker(String aktivBruker) {
        this.aktivBruker = aktivBruker;
        return this;
    }

    public RSContext withAktivEnhet(String aktivEnhet) {
        this.aktivEnhet = aktivEnhet;
        return this;
    }

    public RSContext withIP(String ip) {
        this.ip = ip;
        return this;
    }
}
