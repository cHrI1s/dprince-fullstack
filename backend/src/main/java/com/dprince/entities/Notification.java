package com.dprince.entities;

import com.dprince.apis.utils.DateUtils;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.enums.UserType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldNameConstants;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@FieldNameConstants
@Data
@Entity
@Table(name = "notifications",
        indexes = {
            @Index(name = "index_institution_id", columnList = "institutionId"),
            @Index(name = "index_note_time", columnList = "notificationTime"),
            @Index(name = "index_executor", columnList = "executor"),
        })
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private NotificationActionType actionType;

    /**
     * Example: "New member Created, Company Blocked,
     */
    @Column(nullable = false)
    private String title;

    @Column(length = Integer.MAX_VALUE)
    private String content;

    @JsonIgnore
    private Long executor;

        @Transient
        private String executorFullName;

    @Column(length = 40)
    @Enumerated(value = EnumType.STRING)
    private UserType executorType;

    private Long institutionId;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date notificationTime = DateUtils.getNowDateTime();

    @Column(columnDefinition = "VARCHAR(MAX)")
    private String downloadLink;

    @JsonIgnore
    @Transient
    public String toExcelString(){
        List<String> columns = new ArrayList<>();
        columns.add(this.getActionType().getLabel());
        columns.add(this.getTitle());
        columns.add(this.getContent());

        String dateString = DateUtils.getDateString(notificationTime);
        columns.add(dateString);
        return String.join(",", columns);
    }

    public static Notification createNotification(@Nullable User user,
                                                  @NonNull String title,
                                                  @Nullable String content,
                                                  @NonNull NotificationActionType actionType,
                                                  @Nullable Long institutionId) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setInstitutionId(institutionId);
        if(user!=null) {
            notification.setExecutor(user.getId());
            notification.setExecutorType(user.getUserType());
        } else {
            notification.setExecutorType(UserType.APPLICATION);
        }
        notification.setActionType(actionType);
        return notification;
    }
}
