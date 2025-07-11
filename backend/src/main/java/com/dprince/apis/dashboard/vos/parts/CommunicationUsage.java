package com.dprince.apis.dashboard.vos.parts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommunicationUsage {
    private Long usedSms = 0L;
    private Long usedEmails = 0L;
    private Long usedWhatsapp = 0L;
}
