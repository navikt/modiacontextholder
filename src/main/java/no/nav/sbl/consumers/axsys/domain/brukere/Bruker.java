package no.nav.sbl.consumers.axsys.domain.brukere;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Bruker med historisk ident")
public class Bruker {

    @NotNull
    @ApiModelProperty(example = "Z98765", required = true)
    String appIdent;

    @NotNull
    @ApiModelProperty(example = "100001", required = true)
    Long historiskIdent;
}
