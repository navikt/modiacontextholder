package no.nav.sbl.axsys.tilgang;

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
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ApiModel(description = "Tilganger en applikasjonsbruker har for NAV-enheter")
public class Tilgang implements Serializable {

    @NotNull(message = "Mangler felt enheter")
    @ApiModelProperty(name = "En liste med oversikt over enheter med fagområder", required = true)
    private Set<Enhet> enheter;
}