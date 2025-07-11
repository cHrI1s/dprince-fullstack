package com.dprince.apis.communication;

import com.dprince.apis.communication.models.CommunicatorModel;
import com.dprince.apis.communication.models.TemplateModel;
import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.apis.utils.Messenger;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.configuration.email.EmailService;
import com.dprince.configuration.email.util.EmailToSend;
import com.dprince.configuration.sms.SmsService;
import com.dprince.configuration.sms.models.SmsSendRequest;
import com.dprince.configuration.whatsapp.WhatsappService;
import com.dprince.configuration.whatsapp.utils.WhatsappMessageConfig;
import com.dprince.entities.*;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.enums.TemplateStyle;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/communication")
public class CommunicationController {
    private static final Logger log = LoggerFactory.getLogger(CommunicationController.class);

    private final AppRepository appRepository;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private final SmsService smsService;
    private final WhatsappService whatsappService;



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/send")
    public ApiWorkFeedback saveCommunication(@RequestBody @Valid CommunicatorModel model,
                                             @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(model, this.appRepository);

        Institution institution = this.appRepository
                .getInstitutionRepository()
                .findById(model.getInstitutionId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Institution not recognized."));

        // Raba ko atabibura
        model.checkAll();

        InstitutionCommunication communication = this.objectMapper
                .convertValue(model,
                        InstitutionCommunication.class);
        communication.addPeopleInGroupToDestination(this.appRepository);
        InstitutionCommunication savedCommunication = this.appRepository
                .getCommunicationRepository()
                .save(communication);

        CompletableFuture.runAsync(()->{
            String notificationTitle = "Communication";
            AtomicReference<String> notificationMessageContent = new AtomicReference<>("Failed to send message.");
            AtomicReference<NotificationActionType> notificationType= new AtomicReference<>(NotificationActionType.DELETE);
            try {
                notificationMessageContent.set("Template not Defined!");
                if(savedCommunication.getTemplateId()!=null) {
                    Template messageTemplate = this.appRepository
                            .getTemplateRepository()
                            .findById(savedCommunication.getTemplateId())
                            .orElse(null);

                    notificationMessageContent.set("No Such template found.");
                    if (messageTemplate != null) {
                        String message;
                        if (model.getRecipient() != null && !model.getRecipient().isEmpty()) {
                            switch (model.getWay()) {
                                case SMS:
                                    message = Messenger.formatToSMS(messageTemplate.getSmsText());
                                    notificationMessageContent.set("Message not specified!");
                                    if (!StringUtils.isEmpty(message)) {
                                        SmsSendRequest smsSendRequest = SmsSendRequest.builder()
                                                .message(message)
                                                .numbers(model.getRecipient())
                                                .institutionId(institution.getId())
                                                .directSend(false)
                                                .build();
                                        boolean sent = this.smsService.sendMessage(smsSendRequest);
                                        if (sent) notificationType.set(NotificationActionType.CREATE);
                                        notificationMessageContent.set(sent ? "SMS sent." : "SMS not sent.");
                                    }
                                    break;

                                case MAIL:
                                    message = Messenger.formatToEmail(messageTemplate.getEmailText());
                                    notificationMessageContent.set("Message not specified!");
                                    if (!StringUtils.isEmpty(message)) {
                                        EmailToSend emailToSend = new EmailToSend();
                                        emailToSend.setBcc(model.getRecipient());
                                        emailToSend.setBody(message);
                                        emailToSend.setSubject(model.getSubject());
                                        emailToSend.setInstitution(institution);
                                        emailToSend.setLoggedInUser(loggedInUser);
                                        Boolean sent = this.emailService.sendEmail(emailToSend);
                                        if (sent == null) {
                                            notificationMessageContent.set(null);
                                        } else {
                                            notificationMessageContent.set(sent ? "Email sent." : "Email not sent.");
                                            notificationType.set(NotificationActionType.CREATE);
                                        }
                                    }
                                    break;

                                case WHATSAPP:
                                    model.getRecipient()
                                            .parallelStream()
                                            .forEach(singleNumberString -> {
                                                try {
                                                    InstitutionMember singleMember = new InstitutionMember();
                                                    singleMember.setInstitutionId(institution.getId());
                                                    singleMember.setInstitution(institution);
                                                    singleMember.setFullWhatsappNumber(singleNumberString.trim());
                                                    notificationMessageContent.set("Phone: \"" + singleNumberString + "\" is not a valid number.");
                                                    if (!StringUtils.isEmpty(singleNumberString)) {
                                                        log.info("The language of the template is " + messageTemplate.getLanguage());
                                                        String language = WhatsappMessageConfig.getLanguage(messageTemplate.getLanguage());
                                                        WhatsappMessageConfig waMessageConfig = WhatsappMessageConfig.builder()
                                                                .appRepository(this.appRepository)
                                                                .whatsappService(this.whatsappService)
                                                                .language(language)
                                                                .templateName(messageTemplate.getWhatsappTemplate())
                                                                .templateId(model.getTemplateId())
                                                                .messageTitle(model.getSubject())
                                                                .sender(loggedInUser)
                                                                .build();
                                                        boolean sent = singleMember.sendWhatsappMessage(waMessageConfig);
                                                        notificationMessageContent.set((sent ? "Whatsapp Message sent" : "Whatsapp Message not sent") + " to \"" + singleMember.getFullWhatsappFullNumber() + "\"");
                                                        if (sent) notificationType.set(NotificationActionType.CREATE);
                                                    }
                                                } catch (Exception ignored) {
                                                    ignored.printStackTrace();
                                                }
                                            });
                                    break;
                            }
                        } else {
                            // Rondera abantu yashatse kurungikira email
                            List<Long> memberIds = savedCommunication.getDestination() == null
                                    ? new ArrayList<>()
                                    : savedCommunication.getDestination();
                            if (model.getDestinationGroup() != null) {
                                this.appRepository.getGroupRepository()
                                        .findById(model.getDestinationGroup())
                                        .ifPresent(group -> {
                                            this.appRepository.getGroupMemberRepository()
                                                    .findAllByGroupId(group.getId())
                                                    .parallelStream()
                                                    .forEach(groupMember -> memberIds.add(groupMember.getMemberId()));
                                        });
                            }

                            // Chris NDAYISHIMIYE
                            List<InstitutionMember> memberToText = this.appRepository
                                    .getInstitutionMemberRepository()
                                    .findAllByIdIn(memberIds);
                            memberToText.parallelStream()
                                    .forEach(singleMember -> {
                                        singleMember.setInstitution(institution);
                                        String specificMessage;
                                        switch (model.getWay()) {
                                            case SMS:
                                                notificationMessageContent.set("User\"" + singleMember.getFullName() + "\" does not have an Phone Number.");
                                                specificMessage = Messenger.getMessage(messageTemplate.getSmsText(), singleMember);
                                                specificMessage = Messenger.formatToSMS(specificMessage);
                                                if (singleMember.getPhone() != null) {
                                                    SmsSendRequest smsSendRequest = SmsSendRequest.builder()
                                                            .message(specificMessage)
                                                            .numbers(Collections.singletonList(singleMember.getPhone().toString()))
                                                            .institutionId(institution.getId())
                                                            .directSend(false)
                                                            .build();
                                                    boolean sent = this.smsService.sendMessage(smsSendRequest);
                                                    notificationMessageContent.set((sent ? "SMS sent" : "SMS not sent") + " to \"" + singleMember.getCode() + "\"");
                                                    if (sent) notificationType.set(NotificationActionType.CREATE);
                                                }
                                                break;

                                            case MAIL:
                                                notificationMessageContent.set("User\"" + singleMember.getFullName() + "\" does not have an email address.");
                                                specificMessage = Messenger.getMessage(messageTemplate.getEmailText(), singleMember);
                                                specificMessage = Messenger.formatToEmail(specificMessage);
                                                if (singleMember.getEmail() != null) {
                                                    EmailToSend emailToSend = new EmailToSend();
                                                    emailToSend.setEmail(singleMember.getEmail());
                                                    emailToSend.setSubject(model.getSubject());
                                                    emailToSend.setBody(specificMessage);
                                                    emailToSend.setInstitution(institution);
                                                    emailToSend.setLoggedInUser(loggedInUser);
                                                    Boolean sent = this.emailService.sendEmail(emailToSend);
                                                    if (sent != null) {
                                                        notificationMessageContent.set((sent ? "Email sent" : "Email not sent") + " to \"" + singleMember.getCode() + "\"");
                                                        if (sent) notificationType.set(NotificationActionType.CREATE);
                                                    } else {
                                                        notificationMessageContent.set(null);
                                                    }
                                                }
                                                break;

                                            case WHATSAPP:
                                                notificationMessageContent.set("User\"" + singleMember.getFullName() + "\" does not have an Whatsapp Number.");
                                                if (singleMember.getWhatsappNumber() != null) {
                                                    String language = WhatsappMessageConfig.getLanguage(messageTemplate.getLanguage());
                                                    WhatsappMessageConfig waMessageConfig = WhatsappMessageConfig.builder()
                                                            .appRepository(this.appRepository)
                                                            .whatsappService(this.whatsappService)
                                                            .language(language)
                                                            .templateName(messageTemplate.getName())
                                                            .templateId(model.getTemplateId())
                                                            .messageTitle("Message From " + institution.getName())
                                                            .sender(loggedInUser)
                                                            .build();
                                                    boolean sent = singleMember.sendWhatsappMessage(waMessageConfig);
                                                    notificationMessageContent.set((sent ? "Whatsapp Message sent" : "Whatsapp Message not sent") + " to \"" + singleMember.getFullWhatsappFullNumber() + "\"");
                                                    if (sent) notificationType.set(NotificationActionType.CREATE);
                                                }
                                                break;
                                        }
                                    });
                        }
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
                notificationTitle = "Communication Failed.";
            }

            if(notificationMessageContent.get()!=null) {
                Notification notification = Notification.createNotification(
                        loggedInUser,
                        notificationTitle,
                        savedCommunication.getSubject() + ": " + notificationMessageContent,
                        notificationType.get(),
                        savedCommunication.getInstitutionId()
                );
                this.appRepository.getNotificationsRepository().save(notification);
            }
        });


        return ApiWorkFeedback.builder()
                .message("Sent")
                .successful(true)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/template/save")
    public ApiWorkFeedback saveTemplate(@RequestBody @Valid Template template,
                                        @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(template, this.appRepository);
        // notification
        String message = (template.getId()!=null) ? "Updated" : "Saved";
        List<TemplateStyle> uniqueTemplates = Arrays.asList(TemplateStyle.SMS_RECEIPT,
                TemplateStyle.WHATSAPP_RECEIPT,
                TemplateStyle.EMAIL_RECEIPT);
        if(uniqueTemplates.contains(template.getTemplateStyle())) {
            this.appRepository.getTemplateRepository()
                    .findUniqueTemplate(template.getInstitutionId(), template.getTemplateStyle())
                    .ifPresent(savedTemplate -> template.setId(savedTemplate.getId()));
        }
        Template save = this.appRepository.getTemplateRepository().save(template);
        return  ApiWorkFeedback
                .builder()
                .successful(true)
                .message(message)
                .object(save)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/get/template")
    public ApiWorkFeedback getTemplate(@RequestBody @Valid TemplateModel templateModel,
                                       @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(templateModel, this.appRepository);

        List<Long> institutionIds = new ArrayList<>();
        Institution institution = this.appRepository
                .getInstitutionRepository()
                .findById(templateModel.getInstitutionId())
                .orElseThrow(()->{
                    // Throw this error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Failed to load Institution data.");
                });
        institutionIds.add(institution.getId());
        if(institution.getParentInstitutionId()!=null){
            this.appRepository
                    .getInstitutionRepository()
                    .findAllByParentInstitutionId(templateModel.getInstitutionId())
                    .parallelStream().forEach(childInstitution->{
                        institutionIds.add(childInstitution.getId());
                    });
        }
        List<Template> savedTemplate = this.appRepository.getTemplateRepository()
                .findAllByInstitutionIdIn(institutionIds);
        return ApiWorkFeedback.
                builder()
                .objects(savedTemplate)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @DeleteMapping("/delete-template/{id}")
    public ApiWorkFeedback deleteTemplate(@PathVariable("id") Long id,
                                             @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        Template templateToDelete = this.appRepository.getTemplateRepository()
                .findById(id).orElse(null);
        if(templateToDelete==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "No Such Template found.");
        }

        this.appRepository.getTemplateRepository().deleteById(id);


        String notificationTitle = "Template Deleted";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                "Template: "+templateToDelete.getName()+": Deleted",
                NotificationActionType.DELETE,
                templateToDelete.getId()
        );
        this.appRepository.getNotificationsRepository().save(notification);

        return ApiWorkFeedback.builder()
                .successful(true)
                .message("Deleted!")
                .build();
    }
}