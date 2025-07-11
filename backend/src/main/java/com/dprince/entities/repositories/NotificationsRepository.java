package com.dprince.entities.repositories;

import com.dprince.entities.Notification;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface NotificationsRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByInstitutionIdAndNotificationTimeGreaterThan(@Nullable Long institutionId,
                                                                             @NonNull Date date,
                                                                             @NonNull PageRequest pageRequest);


    @Query("SELECT notif FROM Notification notif" +
            " WHERE notif.executor NOT IN :excludedIds " +
            " AND CONVERT(DATE, notif.notificationTime)>CONVERT(DATE, :date)")
    Page<Notification> getAllVisibleToSuperAdmins(@Nullable List<Long> excludedIds,
                                              @NonNull Date date,
                                              @NonNull PageRequest pageRequest);

    @Transactional
    void deleteAllByNotificationTimeLessThan(Date date);
}
