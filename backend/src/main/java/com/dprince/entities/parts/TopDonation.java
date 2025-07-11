package com.dprince.entities.parts;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TopDonation {
    private Long memberId;
    private Long amount;
    private Long categoryId;
    private Long churchContributionId;
}
