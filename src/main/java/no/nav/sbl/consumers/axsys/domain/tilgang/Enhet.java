package no.nav.sbl.consumers.axsys.domain.tilgang;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "NAV-enhet med fagområder")
public class Enhet implements Serializable {

    @NotNull(message = "Mangler felt enhetId")
    @ApiModelProperty(notes = "4 sifferet enhetsnummer", example = "0200", required = true)
    private String enhetId;

    @NotNull(message = "Mangler felt fagomrader")
    @ApiModelProperty(notes = "Liste over fagområder", example = "['UFO']", required = true)
    private Set<String> fagomrader;

    @ApiModelProperty(notes = "Navn på enhet", example = "NAV Sagene")
    private String navn;
}
