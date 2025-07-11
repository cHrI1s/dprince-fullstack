package com.dprince.configuration.whatsapp;

import com.dprince.apis.utils.AppStringUtils;
import com.dprince.configuration.whatsapp.models.WhatsappMessage;
import com.dprince.configuration.whatsapp.models.part.TemplateComponent;
import com.dprince.configuration.whatsapp.models.part.WhatsappTemplate;
import com.dprince.configuration.whatsapp.models.vos.WhatsappApiResponse;
import com.dprince.configuration.whatsapp.models.vos.WhatsappListTemplateResponse;
import com.dprince.configuration.whatsapp.utils.WhatsappOperationType;
import com.dprince.entities.Institution;
import com.dprince.entities.enums.CommunicationWay;
import com.dprince.entities.utils.AppRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WhatsappService {
    private static final Logger log = LogManager.getLogger(WhatsappService.class);

    private final WhatsappConfiguration configuration;
    private final RestTemplate restTemplate;
    private final AppRepository appRepository;

    @Autowired
    public WhatsappService(WhatsappConfiguration configuration,
                      AppRepository appRepository){
        this.configuration = configuration;
        this.restTemplate = new RestTemplate();
        this.appRepository = appRepository;
    }

    public HttpHeaders getHeaders(boolean isFileUpload){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer "+this.configuration.getApiKey());
        headers.set("X-MYOP-COMPANY-ID", this.configuration.getCompanyID());
        headers.set("Content-Type", isFileUpload ? "multipart/form-data" :"application/json");
        if(!isFileUpload) headers.set("Accept", "application/json");
        return headers;
    }

    public String getUrl(String path, WhatsappOperationType type){
        switch(type){
            default:
            case SEND_MESSAGE:
                return this.configuration.getBaseURL()+path;

            case LIST_TEMPLATE:
                return this.configuration.getTemplatesBaseURL()+path;
        }
    }

    public WhatsappListTemplateResponse getTemplates(){
        try {
            HttpEntity<WhatsappMessage> requestEntity = new HttpEntity<>(null,
                    this.getHeaders(false));
            String url = this.getUrl("/chat/templates", WhatsappOperationType.LIST_TEMPLATE);
            url+="?waba_id="+this.configuration.getWabaId()+"&waba_template_status=approved&limit=25&offset=0";
            ResponseEntity<WhatsappListTemplateResponse> result = this.restTemplate
                    .getForEntity(url, WhatsappListTemplateResponse.class);
            return result.getBody();
        } catch (Exception e){
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            log.info(stringWriter.toString());
            return null;
        }
    }
    public String getTemplate(String templateName){
        WhatsappListTemplateResponse templatesResponse = this.getTemplates();
        List<WhatsappTemplate> foundTemplates = templatesResponse.getResults()
                .parallelStream()
                .filter(template-> template.getName().equals(templateName))
                .collect(Collectors.toList());
        if(foundTemplates.isEmpty())return null;
        WhatsappTemplate theTemplate = foundTemplates.get(0);
        StringBuilder message = new StringBuilder();
        for(TemplateComponent component: theTemplate.getComponents()){
            message.append(component.getText());
        };
        return message.toString();
    }


    /**
     * @description: Will return null if the institution can not Send whatsapp message
     * @param whatsappMessage: <p>This one is an Instance of the WhatsappMessage class.</p>
     * @return: WhatsappApiResponse | null: <p>
     *     Returns either null or an instance of WhatsappApiResponse. null is returned
     *     the system has failed to send the message.
     * </p>
     */
    @Nullable
    public WhatsappApiResponse sendMessage(WhatsappMessage whatsappMessage){
        String operationReferenceId = AppStringUtils.generateRandomString(20);
        whatsappMessage.setPhoneNumberId(this.configuration.getPhoneNumberId());
        whatsappMessage.setMyOpRefId(operationReferenceId);
        Institution institution = null;
        if (whatsappMessage.getInstitution() != null) {
            // Check the institution can send message;
            institution = whatsappMessage.getInstitution();
            boolean canSendWhatsapp = institution.canCommunicate(this.appRepository,
                    CommunicationWay.WHATSAPP);
            if (!canSendWhatsapp) return null;
        }


        try {
            // Make if auto fill
            whatsappMessage.fillInData();

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(whatsappMessage);
            log.info("json=>"+json);
            HttpEntity<WhatsappMessage> requestEntity = new HttpEntity<>(whatsappMessage,
                    this.getHeaders(false));
            String url = this.getUrl("/chat/messages", WhatsappOperationType.SEND_MESSAGE);
            ResponseEntity<WhatsappApiResponse> result = this.restTemplate
                    .exchange(url, HttpMethod.POST, requestEntity, WhatsappApiResponse.class);
            WhatsappApiResponse theResponse = result.getBody();
            if(theResponse!=null && theResponse.getCode().equals("200")){
                if(institution!=null){
                    institution.setWhatsapp(institution.getWhatsapp()+1);
                    this.appRepository.getInstitutionRepository().save(institution);
                }
            }
            return theResponse;
        } catch (Exception e){
            // Internal Server Error
            e.printStackTrace();
            return null;
        }
    }
}
