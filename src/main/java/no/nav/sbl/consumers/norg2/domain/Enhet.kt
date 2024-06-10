package no.nav.sbl.consumers.norg2.domain;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Enhet implements Serializable {
    private String enhetNr;
    private String navn;
    private String status;
}
