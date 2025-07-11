package com.dprince.entities.vos;

import com.dprince.entities.embeded.PersonAddress;
import com.dprince.entities.enums.MemberOfFamilyTitle;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MemberDTO extends PersonAddress {
    private String firstName;
    private String lastName;
    private String code;
    private MemberOfFamilyTitle familyRole;
}
