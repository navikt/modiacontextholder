package no.nav.sbl.db.domain;

import no.nav.sbl.db.converter.LocalDateTimeAttributeConverter;

import javax.persistence.*;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "EVENT")
public class PEvent {

    @Id
    @Column(name = "event_id", updatable = false, nullable = false)
    @SequenceGenerator(name = "eventgenerator", sequenceName = "EVENT_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "eventgenerator")
    private Long id;

    @Column(name = "veileder_ident", nullable = false)
    public String veilederIdent;

    @Column(name = "event_type", nullable = false)
    public String eventType;

    @Column(name = "ip", nullable = false)
    public String ip;

    @Convert(converter = LocalDateTimeAttributeConverter.class)
    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime created = now();

    @Column(name = "verdi", nullable = false)
    public String verdi;

    public Long getId() {
        return id;
    }

    public PEvent withVeilederIdent(String veilederIdent) {
        this.veilederIdent = veilederIdent;
        return this;
    }

    public PEvent withIp(String ip) {
        this.ip = ip;
        return this;
    }

    public PEvent withVerdi(String verdi) {
        this.verdi = verdi;
        return this;
    }

    public PEvent withEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public PEvent withCreated(LocalDateTime created) {
        this.created = created;
        return this;
    }
}
