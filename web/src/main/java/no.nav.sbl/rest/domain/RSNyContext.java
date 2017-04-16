package no.nav.sbl.rest.domain;

public class RSNyContext {
    public String verdi;
    public String ip;
    public String eventType;

    public RSNyContext withVerdi(String verdi) {
        this.verdi = verdi;
        return this;
    }

    public RSNyContext withIp(String ip) {
        this.ip = ip;
        return this;
    }

    public RSNyContext withEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }
}
