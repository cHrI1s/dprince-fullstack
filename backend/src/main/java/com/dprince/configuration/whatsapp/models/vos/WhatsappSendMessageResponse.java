package com.dprince.configuration.whatsapp.models.vos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WhatsappSendMessageResponse extends WhatsappApiResponse{
    @JsonProperty("data")
    public WhatsappApiResponseData data;

    @Data
    public static class WhatsappApiResponseData {
        @JsonProperty("message_id")
        public String messageId;

        @JsonProperty("conversation_id")
        public String conversationId;
    }
}
