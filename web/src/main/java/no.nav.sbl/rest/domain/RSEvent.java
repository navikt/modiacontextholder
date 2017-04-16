package no.nav.sbl.rest.domain;

public class RSEvent {

    public long id;
    public String veilederIdent;
    public String eventType;

    public RSEvent withId(long id) {
        this.id = id;
        return this;
    }

    public RSEvent withVeilederIdent(String veilederIdent) {
        this.veilederIdent = veilederIdent;
        return this;
    }

    public RSEvent withEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }
}
