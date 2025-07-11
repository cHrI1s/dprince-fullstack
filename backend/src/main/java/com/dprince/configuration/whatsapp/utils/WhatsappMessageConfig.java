package com.dprince.configuration.whatsapp.utils;

import com.dprince.configuration.whatsapp.WhatsappService;
import com.dprince.entities.Template;
import com.dprince.entities.User;
import com.dprince.entities.enums.AppLanguage;
import com.dprince.entities.enums.TemplateStyle;
import com.dprince.entities.utils.AppRepository;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Builder
@Data
public class WhatsappMessageConfig {
    private AppRepository appRepository;
    private WhatsappService whatsappService;
    /**
     * If we are acknowledging a payment,
     * the member instance should contain the payment details: The InstitutionReceipt instance
     */
    private TemplateStyle templateStyle;
    private Long templateId;
    private String templateName;
    private String language;
    private Template template;
    private String messageTitle;
    private User sender;


    public static String getLanguage(AppLanguage language){
        Map<AppLanguage, String> languageCodes = new HashMap<>();
        languageCodes.put(AppLanguage.BENGALI, "bn");
        languageCodes.put(AppLanguage.ENGLISH, "en");
        languageCodes.put(AppLanguage.GUJARATI, "gu");
        languageCodes.put(AppLanguage.HINDI, "hi");
        languageCodes.put(AppLanguage.KANNADA, "kn");
        languageCodes.put(AppLanguage.MALAYALAM, "ml");
        languageCodes.put(AppLanguage.MARATHI, "mr");
        languageCodes.put(AppLanguage.PUNJABI, "pa");
        languageCodes.put(AppLanguage.TAMIL, "ta");
        languageCodes.put(AppLanguage.TELUGU, "te");
        languageCodes.put(AppLanguage.URDU, "ur");

        return languageCodes.getOrDefault(language, "en");
    }
}
