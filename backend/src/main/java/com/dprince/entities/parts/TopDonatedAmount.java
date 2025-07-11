package com.dprince.entities.parts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@AllArgsConstructor
@FieldNameConstants
@Data
public class TopDonatedAmount {
    private Long amount;
    private Long memberId;
}
