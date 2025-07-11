package com.dprince.apis.institution.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.InstitutionCreditAccount;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class AccountsCreator implements Institutionable {
    private Long institutionId;

    @Size(min = 1, message = "At least one credit account is to be created")
    @Valid
    private List<InstitutionCreditAccount> creditAccounts;
}
