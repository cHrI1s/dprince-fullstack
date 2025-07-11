package com.dprince.apis.notifications.jobs;

import com.dprince.apis.utils.DateUtils;
import com.dprince.entities.utils.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@RequiredArgsConstructor
@Component
public class NotificationDeletor {
    private final AppRepository appRepository;

    @Scheduled(cron = "0 0 23 * * ?")
    public void runAt11PM() {
        System.out.println("Executing task at 11:00 PM");
        Date minusSevenDays = DateUtils.addDays(-7);
        this.appRepository.getNotificationsRepository()
                .deleteAllByNotificationTimeLessThan(minusSevenDays);
    }
}
