package com.dprince.configuration.whatsapp.models.vos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WhatsappApiResponse {
    @JsonProperty("status")
    public String status;

    @JsonProperty("code")
    public String code;

    @JsonProperty("message")
    public String message;
}
