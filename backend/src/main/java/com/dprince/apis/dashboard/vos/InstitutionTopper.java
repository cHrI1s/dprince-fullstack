package com.dprince.apis.dashboard.vos;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class InstitutionTopper {
    private String name;
    private Long members;
    private Long receipts;
    private Long addresses;
}
