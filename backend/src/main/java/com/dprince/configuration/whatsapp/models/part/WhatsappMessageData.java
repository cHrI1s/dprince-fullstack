package com.dprince.configuration.whatsapp.models.part;

import com.dprince.configuration.whatsapp.models.enums.WhatsappMessageType;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class WhatsappMessageData {
    private WhatsappMessageType type = WhatsappMessageType.text;
    private WhatsappMessageContext context = new WhatsappMessageContext();
    private String language = "en";

    public WhatsappMessageData(WhatsappMessageType type){
        this.type = type;
    }
}
