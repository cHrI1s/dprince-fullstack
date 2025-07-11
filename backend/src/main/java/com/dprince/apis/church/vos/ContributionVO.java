package com.dprince.apis.church.vos;

import com.dprince.entities.InstitutionDonation;
import com.dprince.entities.InstitutionMember;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ContributionVO {
    private InstitutionMember member;
    private List<InstitutionDonation> donations;
}
