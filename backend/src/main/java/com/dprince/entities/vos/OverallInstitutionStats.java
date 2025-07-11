package com.dprince.entities.vos;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class OverallInstitutionStats {
    private Long sms;
    private Long whatsapp;
    private Long emails;
}
