package com.dprince.configuration.whatsapp.models.part;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WhatsappMessageContext {
    public WhatsappMessageContext(String templateName){
        this.templateName = templateName;
    }

    @JsonProperty("template_name")
    private String templateName;


    @JsonProperty("language")
    private String language = "en";

    /**
     * Can be a String of a Map<String, String>
     */
    @JsonProperty("body")
    private Object body;

    @JsonProperty("header")
    private Object header;

    @JsonProperty("preview_url")
    private boolean previewURL;

    @JsonProperty("link")
    private String link;

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("caption")
    private String caption;

    @JsonProperty("mime_type")
    private String mimeType;
}
