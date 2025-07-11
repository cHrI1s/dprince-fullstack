package com.dprince.configuration.whatsapp.models.vos;

import com.dprince.configuration.whatsapp.models.part.WhatsappTemplate;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class WhatsappListTemplateResponse extends WhatsappApiResponse{
    private int count;
    private String next;
    private String previous;
    private List<WhatsappTemplate> results;
}
