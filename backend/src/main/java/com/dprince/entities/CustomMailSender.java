package com.dprince.entities;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.configuration.email.ApplicationMailSender;
import com.dprince.entities.utils.AppRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Properties;

@Data
@Entity
@Table(name = "custom_mail_senders",
        indexes = {
                @Index(name = "index_institution_id", columnList = "institutionId")
        })
public class CustomMailSender implements Institutionable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Long institutionId;

    @NotBlank(message = "Host should not be empty.")
    @Column(nullable = false)
    private String host;

    @Range(min = 20, max=1000, message = "The minimum value for port is 20 and the maximum is 1000")
    private int port;

    @NotBlank(message = "Username should not be empty.")
    @Column(nullable = false)
    private String username;

    @NotBlank(message = "Password should not be empty.")
    @Column(nullable = false)
    private String password;


    @JsonIgnore
    @Transient
    public JavaMailSender getMailSender(){
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(this.getHost());
        mailSender.setPort(this.getPort());

        mailSender.setUsername(this.getUsername());
        mailSender.setPassword(this.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        return mailSender;
    }

    public static ApplicationMailSender getMailSender(@NonNull AppRepository appRepository,
                                               @NonNull JavaMailSender defaultMailSender,
                                               @NonNull Long institutionId) {
        CustomMailSender customMailSender = appRepository
                .getMailSenderRepository()
                .findByInstitutionId(institutionId)
                .orElse(null);
        ApplicationMailSender mailSender = new ApplicationMailSender();
        if(customMailSender!=null) {
            mailSender.setMailSender(customMailSender.getMailSender());
            mailSender.setFrom(customMailSender.getUsername());
            mailSender.setReplyTo(customMailSender.getUsername());
        } else {
            mailSender.setMailSender(defaultMailSender);
        }
        return mailSender;
    }
}
