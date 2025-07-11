package com.dprince.configuration.email;

import com.dprince.configuration.email.util.EmailToSend;
import com.dprince.entities.Institution;
import com.dprince.entities.Notification;
import com.dprince.entities.enums.CommunicationWay;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.utils.AppRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final AppRepository appRepository;

    private ApplicationMailSender applicationMailSender;

    private JavaMailSender getTheMailSender(@Nullable Long institutionId){
        ApplicationMailSender theAppMailSender;
        if(institutionId==null) {
            theAppMailSender = new ApplicationMailSender();
            theAppMailSender.setMailSender(this.mailSender);
            theAppMailSender.setReplyTo("no-reply@dnote.ai");
            theAppMailSender.setFrom("no-reply@dnote.ai");
        } else {
            theAppMailSender = ApplicationMailSender.getSender(institutionId, this.mailSender);
        }
        this.applicationMailSender = theAppMailSender;
        return theAppMailSender.getMailSender();
    }

    /**
     * This should be used for things we do not want to count the number of sms to be sent.
     * Use with precaution.
     * @param toEmail: Destination
     * @param subject: Subject of the mailer
     * @param body: Content of the mailer
     */
    public void sendSimpleEmail(String toEmail, String subject, String body) {
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom("no-reply@dnote.ai"); // Optional: specify a from email
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException ignored) {}
    }


    public Boolean sendEmail(@NonNull EmailToSend emailToSend) {
        Boolean sendEmail = null;
        List<String> mails = new ArrayList<>();
        mails.addAll(emailToSend.getEmails());
        mails.addAll(emailToSend.getBcc());
        try {
            sendEmail = emailToSend.isSystemMail();
            if (emailToSend.getInstitution() != null) {
                sendEmail = emailToSend.getInstitution().canCommunicate(this.appRepository, CommunicationWay.SMS);
            }
            if (sendEmail) {
                JavaMailSender theSender = this.getTheMailSender(emailToSend.getInstitution()==null
                        ? null
                        : emailToSend.getInstitution().getId());
                if(emailToSend.getEmails()==null || emailToSend.getEmails().isEmpty()){
                    // Send the mail to self
                    emailToSend.setEmail(this.applicationMailSender.getReplyTo());
                }
                MimeMessageHelper helper = this.getMimeHelper(emailToSend.getSubject(), emailToSend.getBody());
                helper.setTo(emailToSend.getEmails().toArray(new String[0]));
                if (!emailToSend.getBcc().isEmpty()) helper.setBcc(emailToSend.getBcc().toArray(new String[0]));
                emailToSend.getAttachments().keySet()
                        .parallelStream()
                        .forEach(fileName -> {
                            try {
                                File attachmentFile = emailToSend.getAttachments().get(fileName);
                                if (attachmentFile != null) {
                                    helper.addAttachment(fileName,
                                            emailToSend.getAttachments().get(fileName));
                                }
                            } catch (Exception ignored) {
                            }
                        });
                theSender.send(helper.getMimeMessage());

                String content = "Successfully sent Email to "+ String.join(",", mails);
                Notification notification = Notification
                        .createNotification(emailToSend.getLoggedInUser()==null ? null : emailToSend.getLoggedInUser(),
                                "Email sent",
                                content,
                                NotificationActionType.CREATE,
                                emailToSend.getInstitution()==null ? null : emailToSend.getInstitution().getId());
                this.appRepository.getNotificationsRepository().save(notification);

                if(emailToSend.getInstitution()!=null){
                    Institution institution = emailToSend.getInstitution();
                    institution.setEmails(institution.getEmails()+1);
                    this.appRepository.getInstitutionRepository().save(institution);
                }

                this.updateInstitutionUsage(emailToSend);
                sendEmail = true;
            }
        } catch (Exception e){
            e.printStackTrace();
            Notification notification = Notification
                    .createNotification(emailToSend.getLoggedInUser()==null ? null : emailToSend.getLoggedInUser(),
                            "Email sent",
                            "Failed to send email to "+String.join(", "+mails)+". An Unexpected error occured.",
                            NotificationActionType.DELETE,
                            emailToSend.getInstitution()==null ? null : emailToSend.getInstitution().getId());
            this.appRepository.getNotificationsRepository().save(notification);
        }
        return sendEmail;
    }


    private MimeMessageHelper getMimeHelper(@NonNull String subject,
                                            @NonNull String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setSubject(subject);
        helper.setText(body, true); // `true` if the text contains HTML

        helper.setFrom("no-reply@dnote.ai");
        helper.setReplyTo("no-reply@dnote.ai");
        if(this.applicationMailSender!=null){
            if(!StringUtils.isEmpty(this.applicationMailSender.getFrom())) helper.setFrom(this.applicationMailSender.getFrom());
            if(!StringUtils.isEmpty(this.applicationMailSender.getReplyTo())) helper.setReplyTo(this.applicationMailSender.getReplyTo());
        }
        return helper;
    }



    /**
     * This method should be used only when the email to send is from the Application Owner,
     * not the institution. This method is not equipped with the mechanism to update the
     * number of emails an institution should have once the email is sent.
     * @apiNote To avoid that institutions fail to see their Email
     * Packages not updated please do not use this method.
     * This is exclusively meant to be used by the System or Software Owner to send mails.
     * @param email: A String that holds the email to which the mailer should be sent
     * @param subject: Subject of the mailer
     * @param body: Body of the Email
     */
    public boolean applicationSendHTMLEmail(String email,
                                         String subject,
                                         @Nullable Map<String, byte[]> attachments,
                                         String body) {
        try {
            MimeMessageHelper helper = this.getMimeHelper(subject, body);
            helper.setTo(email);
            if (attachments != null) {
                Set<String> attachmentNames = attachments.keySet();
                attachmentNames.parallelStream()
                        .forEach(attachmentName -> {
                            try {
                                InputStreamSource bis = new ByteArrayResource(attachments.get(attachmentName));
                                helper.addAttachment(attachmentName, bis);
                            } catch (MessagingException ignored) {
                            }
                        });
            }
            mailSender.send(helper.getMimeMessage());
            return true;
        } catch (MessagingException ignored){
            return false;
        }
    }



    private void updateInstitutionUsage(@NonNull EmailToSend email){
        if(email.getInstitution()!=null && email.getLoggedInUser()!=null){
            email.getInstitution().setEmails(email.getInstitution().getEmails()+1);
        }
    }
}