package com.dprince.apis.cron;

import com.dprince.apis.cron.models.ReportSchedulerModel;
import com.dprince.apis.cron.task.InstitutionBackupTask;
import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.apis.utils.models.SimpleGetModel;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.configuration.database.services.DatabaseBackupService;
import com.dprince.configuration.files.LocalFileStorageService;
import com.dprince.entities.CronJob;
import com.dprince.entities.Institution;
import com.dprince.entities.User;
import com.dprince.entities.enums.CronInitiator;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import com.dprince.scheduler.manager.DynamicTaskManager;
import com.dprince.scheduler.task.ReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/cron")
public class CronJobController {
    private final AppRepository appRepository;
    private final DataSource dataSource;
    private final DatabaseBackupService databaseBackupService;
    private final LocalFileStorageService storageService;
    private final DynamicTaskManager dynamicTaskManager;
    private final ReportGenerator reportGenerator;


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/create")
    public ApiWorkFeedback createCronJob(@Valid @RequestBody ReportSchedulerModel schedulerModel,
                                         @NonNull HttpSession session) {
        final String cronExpression = schedulerModel.getCronExpression();
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(schedulerModel, this.appRepository);
        CronInitiator initiator = User.noOrganizationUsers().contains(loggedInUser.getUserType())
                ? CronInitiator.SUPER_ADMIN
                : CronInitiator.ADMIN;

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(schedulerModel.getInstitutionId())
                .orElseThrow(()->{
                    // Throw an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Institution is not recognized");
                });

        CronJob oldCronJob = this.appRepository.getCronJobRepository()
                .findByInstitutionId(institution.getId());
        if(oldCronJob!=null){
            dynamicTaskManager.removeTask(oldCronJob.getTaskId());
            this.appRepository.getCronJobRepository().delete(oldCronJob);
        }

        CronJob cronJob = new CronJob();
        cronJob.setScheduleTime(schedulerModel.getScheduleTime());
        cronJob.setCronExpression(cronExpression);
        cronJob.setInstitutionId(schedulerModel.getInstitutionId());
        cronJob.setMinutes(schedulerModel.getMinutes());
        cronJob.setHours(schedulerModel.getHours());
        cronJob.setTaskInitiator(initiator);
        CronJob savedCronJob = this.appRepository.getCronJobRepository()
                .save(cronJob);

        CronTrigger trigger = new CronTrigger(cronExpression);
        InstitutionBackupTask backupTask = new InstitutionBackupTask();
        backupTask.setAppRepository(this.appRepository);
        backupTask.setDataSource(this.dataSource);
        backupTask.setDatabaseBackupService(this.databaseBackupService);
        backupTask.setStorageService(this.storageService);
        backupTask.setReportGenerator(this.reportGenerator);
        backupTask.setInstitution(institution);
        backupTask.setInitiator(initiator);
        // add to tasks


        dynamicTaskManager.addTask(savedCronJob.getTaskId(), backupTask, trigger);
        return ApiWorkFeedback.builder()
                .message("Scheduler set!")
                .object(cronJob)
                .build();
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/get")
    public ApiWorkFeedback getCronJob(@Valid @RequestBody SimpleGetModel getModel,
                                         @NonNull HttpSession session) {
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(getModel, this.appRepository);

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(getModel.getInstitutionId())
                .orElseThrow(()->{
                    // Throw an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Institution is not recognized");
                });

        boolean successful = true;
        CronJob savedCron = this.appRepository.getCronJobRepository()
                .findByInstitutionId(institution.getId());
        if(savedCron==null) {
            savedCron = new CronJob();
            successful = false;
        }


        return ApiWorkFeedback.builder()
                .message(successful ? "Scheduler retrieved" : "No Scheduler found")
                .successful(successful)
                .object(savedCron)
                .build();
    }
}