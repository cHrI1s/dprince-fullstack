package com.dprince.scheduler.task;

import com.dprince.configuration.ApplicationConfig;
import com.dprince.configuration.database.services.DatabaseBackupService;
import com.dprince.configuration.email.EmailService;
import com.dprince.configuration.email.util.EmailToSend;
import com.dprince.configuration.files.LocalFileStorageService;
import com.dprince.entities.FileToken;
import com.dprince.entities.Institution;
import com.dprince.entities.Notification;
import com.dprince.entities.User;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BackupGenerator {
    private final EntityManager entityManager;
    private final AppRepository appRepository;
    private final EmailService emailService;
    private final LocalFileStorageService storageService;
    private final DatabaseBackupService databaseBackupService;


    public void backup(@Nullable String databaseName,
                       @Nullable Institution institution){
        FileToken fileToken = databaseBackupService.backupDatabase(storageService, databaseName);
        if(fileToken!=null){
            final FileToken savedFileToken = this.appRepository.getFileTokensRepository().save(fileToken);
            List<User> superAdmins = this.appRepository.getUsersRepository()
                    .findAllByUserTypeIn(Collections.singletonList(UserType.SUPER_ADMINISTRATOR));
            if(!superAdmins.isEmpty()){
                List<String> superAdminsEmails = superAdmins.parallelStream()
                        .map(User::getEmail)
                        .collect(Collectors.toList());

                String body = this.getBackupEmailContent(savedFileToken, institution);

                String emailSubject = (institution!=null)
                        ? institution.getName()+" backup"
                        : "Database Automatic Backup Report.";
                try {
                    EmailToSend emailToSend = new EmailToSend();
                    emailToSend.setBody(body);
                    emailToSend.setEmails(superAdminsEmails);
                    emailToSend.setInstitution(null);
                    emailToSend.setSubject(emailSubject);
                    emailToSend.setLoggedInUser(null);
                    emailToSend.setSystemMail(true);
                    this.emailService.sendEmail(emailToSend);

                    Notification notification = Notification
                            .createNotification(null,
                                    "Database Automatic Backup Report.",
                                    body,
                                    NotificationActionType.CREATE, null);
                    this.appRepository.getNotificationsRepository().save(notification);
                } catch (Exception exception){
                    exception.printStackTrace();
                    Notification notification = Notification
                            .createNotification(null,
                                    emailSubject+": Failed to send email",
                                    body,
                                    NotificationActionType.CREATE, null);
                    this.appRepository.getNotificationsRepository().save(notification);
                }
            }
        }
    }

    @NotNull
    private String getBackupEmailContent(@NotNull FileToken savedFileToken,
                                         @Nullable Institution institution) {
        String link = ApplicationConfig.APP_URL+"/files/get-file/"+ savedFileToken.getToken()+"/"+ savedFileToken.getFileName();
        if(institution==null) {
            return "<div>" +
                    "Dear Super Administrator, we are glad to let you know that" +
                    " we have performed a backup of the entire database. You can find it" +
                    " at this link <a href='" + link + "' download>Download the Backup</a>" +
                    "</div>" +
                    "<div>" +
                    "This backup expires within 24hours from now." +
                    "</div>";
        }

        return "<div>" +
                "Dear Super Administrator, we are glad to let you know that" +
                " we have performed a backup of the \""+institution.getName()+"\". You can find it" +
                " at this link <a href='" + link + "'>Download the Backup</a>" +
                "</div>" +
                "<div>" +
                "This backup expires within 24hours from now." +
                "</div>";
    }
}
