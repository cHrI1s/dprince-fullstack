package com.dprince.apis.institution.vos;

import com.dprince.entities.embeded.PersonAddress;
import lombok.Data;

@Data
public class AddressCard {
    private String firstName;
    private String lastName;
    private PersonAddress address;
    private String code;
}
