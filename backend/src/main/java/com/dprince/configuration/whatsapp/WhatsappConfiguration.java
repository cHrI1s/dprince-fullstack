package com.dprince.configuration.whatsapp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "whatsapp")
public class WhatsappConfiguration {
    private String apiKey;
    private String baseURL;
    private String templatesBaseURL;
    private String companyID;
    private String phoneNumberId;
    private String wabaId;
}
