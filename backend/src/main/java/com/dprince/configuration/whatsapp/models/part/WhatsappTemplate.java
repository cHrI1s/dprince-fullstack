package com.dprince.configuration.whatsapp.models.part;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class WhatsappTemplate {
    private UUID id;
    private String name;

    @JsonProperty("waba_template_id")
    private String templateId;

    private String language;

    private String category;

    private String type;
    private String quality;
    private String status;

    @JsonProperty("waba_template_status")
    private String templateStatus;

    @JsonProperty("failure_reason")
    private String failureReason;

    private Date created;
    private Date modified;

    private List<TemplateComponent> components;
}
