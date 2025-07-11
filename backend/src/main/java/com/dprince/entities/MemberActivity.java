package com.dprince.entities;

import com.dprince.apis.utils.DateUtils;
import com.dprince.entities.utils.AppRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "member_activites",
        indexes = {
            @Index(name = "index_institution_id", columnList = "institutionId")
        })
public class MemberActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long institutionId;

    @Column(nullable = false)
    private Date lastActivityTime = DateUtils.getNowDateTime();
    private void updateLastActivityTime(){
        this.setLastActivityTime(DateUtils.getNowDateTime());
    }


    public static void saveActivity(@NonNull AppRepository appRepository,
                                    @NonNull Long memberId,
                                    @NonNull Long institutionId){
        MemberActivity savedActivity = MemberActivity.getLastActivity(
                appRepository,
                memberId,
                institutionId,
                true);
        savedActivity.updateLastActivityTime();
        appRepository.getMemberActivityRepository().save(savedActivity);
    }

    @JsonIgnore
    public static MemberActivity getLastActivity(@NonNull AppRepository appRepository,
                                                 @NonNull Long memberId,
                                                 @NonNull Long institutionId,
                                                 boolean returnNew){
        return appRepository
                .getMemberActivityRepository()
                .findByMemberId(memberId)
                .orElseGet(()->{
                    if(returnNew) {
                        MemberActivity activity = new MemberActivity();
                        activity.setMemberId(memberId);
                        activity.setInstitutionId(institutionId);
                        return activity;
                    }
                    return null;
                });
    }
}
