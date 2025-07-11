package com.dprince.configuration.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "sms")
public class SmsConfiguration {
    private String apiKey;
    private String clientId;
    private String user;
    private String password;
    private String channel;
    private int route;
    private int DCS;
    private int flashsms;
}
