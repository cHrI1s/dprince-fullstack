package com.dprince.apis.institution.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

@Data
public class FamilyProspectiveMemberSearcher implements Institutionable {
    private Long institutionId;
    private String query;
    private String code;
}
