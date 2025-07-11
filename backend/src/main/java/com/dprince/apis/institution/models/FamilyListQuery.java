package com.dprince.apis.institution.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.apis.utils.models.ListQuery;
import com.dprince.apis.utils.models.capabilities.Internationalizable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FamilyListQuery extends ListQuery implements Institutionable, Internationalizable {
    private Long institutionId;
    private List<String> memberCodes;

    @NotEmpty(message = "We could not locate your device. Please make sure your device is properly set.")
    private String clientTimezone = "Asia/Calcutta";
}
