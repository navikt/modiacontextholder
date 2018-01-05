package no.nav.sbl.rest.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class RSNyContext {
    public String verdi;
    public String eventType;
}
