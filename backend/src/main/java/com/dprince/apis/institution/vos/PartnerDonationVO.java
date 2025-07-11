package com.dprince.apis.institution.vos;

import com.dprince.entities.InstitutionReceipt;
import com.dprince.entities.vos.PageVO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Builder
@Data
public class PartnerDonationVO {
    private long total;
    private PageVO receipts;
    @JsonIgnore
    private Page<InstitutionReceipt> receiptsOriginal;
}
