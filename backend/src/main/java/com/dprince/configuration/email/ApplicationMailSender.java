package com.dprince.configuration.email;

import com.dprince.entities.CustomMailSender;
import com.dprince.entities.utils.AppRepository;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ApplicationMailSender {
    private static Map<Long, CustomMailSender> MAIL_SENDERS = new HashMap<>();
    public static void addCustomMailSender(@NonNull CustomMailSender mailSender){
        ApplicationMailSender.MAIL_SENDERS.put(mailSender.getInstitutionId(), mailSender);
    }
    public static ApplicationMailSender getSender(@NonNull Long institutionId,
                                                  @NonNull JavaMailSender defaultMailSender){
        ApplicationMailSender theSender  = new ApplicationMailSender();
        if(!MAIL_SENDERS.containsKey(institutionId)){
            theSender.setMailSender(defaultMailSender);
            theSender.setFrom("no-reply@dnote.ai");
            theSender.setReplyTo("no-reply@dnote.ai");
        } else {
            CustomMailSender theCustomSender = MAIL_SENDERS.get(institutionId);
            JavaMailSender theMailSender = theCustomSender.getMailSender();
            theSender.setMailSender(theMailSender);
            theSender.setFrom(theCustomSender.getUsername());
            theSender.setReplyTo(theCustomSender.getUsername());
        }
        return theSender;
    }
    public static void initializeMailSenders(@NonNull AppRepository appRepository){
        List<CustomMailSender> customMailers = appRepository.getMailSenderRepository().findAll();
        Map<Long, CustomMailSender> senders = customMailers.parallelStream()
                .collect(Collectors.toMap(CustomMailSender::getInstitutionId, item->item));
        if(!senders.isEmpty()) ApplicationMailSender.MAIL_SENDERS = senders;
    }



    private JavaMailSender mailSender;
    private String replyTo = "no-reply@dnote.ai";
    private String from = "no-reply@dnote.ai";
}
