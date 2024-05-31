package no.nav.sbl.consumers.axsys.domain.brukere;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Bruker {
    String appIdent;
    Long historiskIdent;
}
