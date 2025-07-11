package com.dprince.entities.vos;

import com.dprince.entities.InstitutionMember;
import com.dprince.entities.enums.MemberOfFamilyTitle;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class FamilyMemberDBO extends InstitutionMember {
    private Long familyId;
    private MemberOfFamilyTitle familyTitle;

    public FamilyMemberDBO(InstitutionMember member,
                           Long familyId,
                           MemberOfFamilyTitle familyTitle){
        BeanUtils.copyProperties(member, this);
        this.setFamilyId(familyId);
        this.setFamilyTitle(familyTitle);
    }
}
