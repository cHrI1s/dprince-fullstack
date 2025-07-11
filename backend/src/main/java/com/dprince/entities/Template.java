package com.dprince.entities;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.configuration.database.converters.ListOfCommunicativeWaysConverter;
import com.dprince.entities.enums.AppLanguage;
import com.dprince.entities.enums.CommunicationWay;
import com.dprince.entities.enums.TemplateStyle;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Entity
@Table(name = "messages_templates",
        indexes = {
            @Index(name = "idx_institution_id", columnList = "institutionId")
        })
public class Template implements Institutionable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = "Please specify a communication way for this templet.")
    @Size(min = 1, message = "At least one communication way must be defined.")
    @Column(nullable = false)
    @Convert(converter = ListOfCommunicativeWaysConverter.class)
    private List<CommunicationWay> communicationWays;

    @NotBlank(message = "The name of the template is not provided.")
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long institutionId;

    @NotNull(message = "The style should be specified.")
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TemplateStyle templateStyle;

    @Column(length = Integer.MAX_VALUE)
    private String emailText;

    @Column(length = Integer.MAX_VALUE)
    private String smsText;

    @Column(length = Integer.MAX_VALUE)
    private String whatsappTemplate;

    @Column(length = Integer.MAX_VALUE)
    private String whatsappText;

    @Column(nullable = false)
    private Boolean smsApproved = false;

    @Column(length = 15)
    @Enumerated(EnumType.STRING)
    private AppLanguage language = AppLanguage.ENGLISH;
}
