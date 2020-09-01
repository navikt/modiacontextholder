package no.nav.sbl.consumers.axsys.domain.tilgang;

import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Tilgang implements Serializable {
    private Set<Enhet> enheter;
}
