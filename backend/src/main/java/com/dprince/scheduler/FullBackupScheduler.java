package com.dprince.scheduler;

import com.dprince.entities.utils.AppRepository;
import com.dprince.scheduler.task.BackupGenerator;
import com.dprince.scheduler.task.ReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FullBackupScheduler {
    private final BackupGenerator backupGenerator;
    private final ReportGenerator reportGenerator;
    private final AppRepository appRepository;


    @Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
    public void getServerBackup(){
        this.backupGenerator.backup(null, null);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void generateReport(){
        this.appRepository.getInstitutionRepository()
                .findAll()
                .parallelStream()
                .forEach(reportGenerator::generateReport);
    }
}
