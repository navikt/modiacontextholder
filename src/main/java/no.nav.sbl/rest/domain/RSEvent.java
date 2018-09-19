package no.nav.sbl.rest.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class RSEvent {
    public long id;
    public String veilederIdent;
    public String eventType;

}
