package com.dprince.apis.institution.models;

import com.dprince.apis.institution.models.enums.DeadlineType;
import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.apis.utils.models.ListQuery;
import com.dprince.apis.utils.models.enums.BlockSelector;
import com.dprince.entities.User;
import com.dprince.entities.enums.InstitutionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class InstitutionListing extends ListQuery implements Institutionable {
    @NotNull(message = "Please specify the institution type")
    private InstitutionType institutionType;

    private Long category;

    private DeadlineType deadlineType = DeadlineType.ALL;

    @NotNull(message = "The Block status should be specify.")
    private BlockSelector blockSelector = BlockSelector.ALL;

    private Long institutionId;

    private Long phone;

    @JsonIgnore
    private User loggedInUser;

}
