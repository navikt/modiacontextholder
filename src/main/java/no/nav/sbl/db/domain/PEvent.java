package no.nav.sbl.db.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class PEvent {
    public Long id;
    public String veilederIdent;
    public String eventType;
    public LocalDateTime created;
    public String verdi;
}
