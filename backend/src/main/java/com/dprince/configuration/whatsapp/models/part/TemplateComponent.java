package com.dprince.configuration.whatsapp.models.part;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class TemplateComponent {
    private String type;
    private String format;

    @JsonProperty("media_id")
    private String mediaId;

    private String text;
    private Map<String, String> example;
}
