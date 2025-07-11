package com.dprince.configuration.sms;

import com.dprince.configuration.sms.models.SmsSendRequest;
import com.dprince.configuration.sms.packages.SmsUtils;
import com.dprince.configuration.sms.vos.SmsBalance;
import com.dprince.configuration.sms.vos.SmsDataOutput;
import com.dprince.entities.Institution;
import com.dprince.entities.enums.CommunicationWay;
import com.dprince.entities.utils.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@Service
public class SmsService {
    private static final String BASE_URL = "http://retailsms.nettyfish.com/api/mt/";
    private static final String SENDER_ID = "DNOTEI";

    private final SmsConfiguration smsConfiguration;
    private final RestTemplate restTemplate;
    private final AppRepository appRepository;

    public UriComponentsBuilder getBaseUriComponent(String url){
        return UriComponentsBuilder.fromUriString(BASE_URL + url)
                .queryParam("user", this.smsConfiguration.getUser())
                .queryParam("password", this.smsConfiguration.getPassword());
    }

    public UriComponentsBuilder getUriComponent(String url){
        return this.getBaseUriComponent(url)
                .queryParam("senderid", SENDER_ID)
                .queryParam("channel", this.smsConfiguration.getChannel())
                .queryParam("DCS", this.smsConfiguration.getDCS())
                .queryParam("flashsms", 0)
                .queryParam("route", this.smsConfiguration.getRoute());
    }


    public boolean sendMessage(SmsSendRequest smsSendRequest){
        Institution institution = null;
        if(!smsSendRequest.isDirectSend()) {
            if (smsSendRequest.getInstitutionId() != null) {
                // Check the institution can send message;
                institution = this.appRepository.getInstitutionRepository()
                        .findById(smsSendRequest.getInstitutionId())
                        .orElse(null);
                if (institution == null) return false;
                if(institution.getParentInstitutionId()!=null){
                    institution = this.appRepository.getInstitutionRepository()
                            .findById(institution.getParentInstitutionId())
                            .orElse(null);
                    if (institution == null) return false;
                }
                boolean canSendSms = institution.canCommunicate(this.appRepository,
                        CommunicationWay.SMS);
                if (!canSendSms) return false;
            }
        }

        boolean sent = false;
        URI uri = this.getUriComponent("SendSMS")
                .queryParam("number", String.join(",", smsSendRequest.getNumbers()))
                .queryParam("text", smsSendRequest.getMessage())
                .build()
                .toUri();
        ResponseEntity<SmsDataOutput> result = this.restTemplate.getForEntity(uri, SmsDataOutput.class);
        SmsDataOutput output = result.getBody();
        if(output != null) sent = output.getErrorMessage().equals("Done");

        int smsesSent = SmsUtils.countSmses(smsSendRequest.getMessage().length());
        if(institution!=null && sent){
            institution.setSmses(institution.getSmses()+smsesSent);
            this.appRepository.getInstitutionRepository().save(institution);
        }
        return sent;
    }

    public SmsBalance getBalance(){
        try {
            URI uri = this.getBaseUriComponent("GetBalance").build().toUri();
            ResponseEntity<SmsBalance> result = this.restTemplate.getForEntity(uri, SmsBalance.class);
            SmsBalance output = result.getBody();
            if (output == null) return SmsBalance.getFlatInstance();
            if (!output.getErrorMessage().equals("Done")) return SmsBalance.getFlatInstance();
            return output;
        } catch(Exception e) {
            e.printStackTrace();
            return new SmsBalance();
        }
    }
}
