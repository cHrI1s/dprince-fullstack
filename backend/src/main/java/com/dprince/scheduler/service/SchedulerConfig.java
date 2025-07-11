package com.dprince.scheduler.service;

import com.dprince.apis.cron.task.InstitutionBackupTask;
import com.dprince.configuration.database.services.DatabaseBackupService;
import com.dprince.configuration.email.ApplicationMailSender;
import com.dprince.configuration.files.LocalFileStorageService;
import com.dprince.entities.CronJob;
import com.dprince.entities.utils.AppRepository;
import com.dprince.startup.GeneralValues;
import com.dprince.scheduler.task.ReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SchedulerConfig implements SchedulingConfigurer {
    private final AppRepository appRepository;
    private final DataSource dataSource;
    private final DatabaseBackupService databaseBackupService;
    private final LocalFileStorageService storageService;
    private final ReportGenerator reportGenerator;


    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        List<CronJob> cronJobList = this.appRepository.getCronJobRepository().findAll();
        cronJobList.parallelStream()
                        .forEach(cronJob -> {
                            this.appRepository.getInstitutionRepository()
                                    .findById(cronJob.getId())
                                    .ifPresent(institution->{
                                        CronTrigger trigger = new CronTrigger(cronJob.getCronExpression());
                                        InstitutionBackupTask backupTask = new InstitutionBackupTask();
                                        backupTask.setInstitution(institution);
                                        backupTask.setDataSource(this.dataSource);
                                        backupTask.setAppRepository(this.appRepository);
                                        backupTask.setDatabaseBackupService(this.databaseBackupService);
                                        backupTask.setStorageService(this.storageService);
                                        backupTask.setReportGenerator(this.reportGenerator);
                                        taskRegistrar.addTriggerTask(backupTask, trigger);
                                    });
                        });


        GeneralValues.initialize(appRepository);
        ApplicationMailSender.initializeMailSenders(appRepository);
    }
}