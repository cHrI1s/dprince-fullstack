package com.dprince.apis.institution.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.apis.utils.models.ListQuery;
import com.dprince.entities.Institution;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdminListingQuery extends ListQuery implements Institutionable {
    private Long groupId;
    private Long institutionId;

    @JsonIgnore
    private Institution institution;
}
