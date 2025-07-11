package com.dprince.apis.institution.models.files;

import com.dprince.apis.utils.AppStringUtils;
import com.dprince.apis.utils.DateUtils;
import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.configuration.ApplicationConfig;
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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.springframework.lang.Nullable;

import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.Reader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ExcelImporter implements Institutionable {
    private Long institutionId;

    @NotBlank(message = "Please specify the file name.")
    private String name;

    private String excelHeaders;

    @JsonIgnore
    @Transient
    private Institution institution;

    @JsonIgnore
    private User loggedInUser;

    @JsonIgnore
    private Reader reader;

    @JsonIgnore
    private AppRepository appRepository;

    @JsonIgnore
    private DateTimeFormatter dateFormatter;

    @JsonIgnore
    private LocalFileStorageService storageService;

    @JsonIgnore
    private EmailService emailService;

    @JsonIgnore
    private ObjectMapper objectMapper;

    @JsonIgnore
    private DateTimeFormatter dateTimeFormatter;

    @JsonIgnore
    public Date getDateFromString(String dateString){
        if (this.getDateFormatter() == null) this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dobLocalDate = LocalDate.parse(dateString, this.getDateFormatter());
        return Date.from(dobLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }


    @JsonIgnore
    private String defaultHeader;
    public Notification generateNotification(@Nullable Integer position,
                                              String message){
        return Notification.
                createNotification(
                        this.getLoggedInUser(),
                        position!=null ? (this.getDefaultHeader()+" : "+position) : this.getDefaultHeader().trim(),
                        message,
                        NotificationActionType.DELETE,
                        User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                ? null
                                : this.getInstitutionId());
    }

    protected void saveIntoFile(List<Notification> notifications){
        try {
            List<String> notificationsStrings = new ArrayList<>();
            notificationsStrings.add(this.getExcelHeaders());
            notifications.parallelStream()
                    .forEach(singleNotification -> {
                        notificationsStrings.add(singleNotification.toExcelString());
                    });

            String fileName = AppStringUtils.generateRandomString(20);
            File file = new File(this.getStorageService().getFileConfig().getBackupDir()+fileName + ".csv");

            FileUtils.writeLines(file, notificationsStrings);

            Date tomorrow = DateUtils.atTheEndOfTheDay(DateUtils.addDays(1));
            FileToken fileToken = new FileToken();
            fileToken.generateToken();
            fileToken.setFileName(fileName+".csv");
            fileToken.setExpirationDate(tomorrow);
            FileToken savedToken = this.getAppRepository().getFileTokensRepository()
                    .save(fileToken);

            Notification notification = new Notification();
            notification.setTitle("Download Link of the status of the import");
            notification.setContent("Please find the link to the status of the upload.");
            notification.setExecutorType(this.getLoggedInUser().getUserType());
            notification.setActionType(NotificationActionType.READ);
            notification.setExecutor(this.getLoggedInUser().getId());
            notification.setInstitutionId(this.getInstitution().getId());
            final String fileDownloadLink = savedToken.getToken()+"/"+fileName+".csv";
            notification.setDownloadLink(fileDownloadLink);
            this.getAppRepository().getNotificationsRepository()
                    .save(notification);

            this.acknowledgeFinish(fileDownloadLink);
        } catch (Exception ignored){ }
    }



    private void acknowledgeFinish(String fileDownloadLink){
        if(this.getEmailService()!=null) {
            List<UserType> userTypes = User.noOrganizationUsers();
            List<String> adminsEmails = this.getAppRepository().getUsersRepository()
                    .findAllByUserTypeIn(userTypes)
                    .parallelStream()
                    .map(User::getEmail)
                    .collect(Collectors.toList());

            EmailToSend emailToSend = new EmailToSend();
            emailToSend.setLoggedInUser(this.getLoggedInUser());
            emailToSend.setSubject(this.getDefaultHeader());
            emailToSend.setEmails(adminsEmails);
            String body = "Please find the link to the status of the upload." +
                    "<a title='Download link' href='" + ApplicationConfig.APP_URL + "/files/get-file/" + fileDownloadLink + "'>Download here</a>";
            emailToSend.setBody(body);
            this.getEmailService().sendEmail(emailToSend);
        }
    }
}
