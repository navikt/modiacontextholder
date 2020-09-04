package no.nav.sbl.consumers.axsys.domain.tilgang;

import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Enhet implements Serializable {
    private String enhetId;
    private Set<String> fagomrader;
    private String navn;
}
