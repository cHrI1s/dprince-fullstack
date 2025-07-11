package com.dprince.apis.institution.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

@Data
public class InstitutionMemberSearchModel  implements Institutionable {
    private Long institutionId;
}
