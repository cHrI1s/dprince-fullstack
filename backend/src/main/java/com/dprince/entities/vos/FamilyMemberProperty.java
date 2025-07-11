package com.dprince.entities.vos;

import com.dprince.entities.enums.MemberOfFamilyTitle;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FamilyMemberProperty {
    private Long memberId;
    private MemberOfFamilyTitle title;
}
