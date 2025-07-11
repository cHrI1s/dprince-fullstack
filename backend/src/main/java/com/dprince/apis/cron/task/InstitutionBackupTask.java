package com.dprince.apis.cron.task;

import com.dprince.apis.utils.AppStringUtils;
import com.dprince.configuration.database.services.DatabaseBackupService;
import com.dprince.configuration.files.LocalFileStorageService;
import com.dprince.entities.Institution;
import com.dprince.entities.Notification;
import com.dprince.entities.User;
import com.dprince.entities.enums.CronInitiator;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.utils.AppRepository;
import com.dprince.scheduler.task.BackupGenerator;
import com.dprince.scheduler.task.ReportGenerator;
import lombok.Data;
import lombok.NonNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Data
public class InstitutionBackupTask implements Runnable {
    private DataSource dataSource;
    private AppRepository appRepository;
    private Institution institution;
    private DatabaseBackupService databaseBackupService;
    private LocalFileStorageService storageService;
    private BackupGenerator backupGenerator;
    private ReportGenerator reportGenerator;
    private CronInitiator initiator;

    @Override
    public void run() {
        if(this.getInstitution()!=null){
            if(this.initiator.equals(CronInitiator.SUPER_ADMIN)) this.doDataBackup();
            else reportGenerator.generateReport(this.getInstitution());
        }
    }

    private void backupDbFile(String databaseName, Institution institution){
        this.backupGenerator.backup(databaseName, institution);
    }

    private void doDataBackup(){
        String dbUniqueName = "database_bkp_"+ AppStringUtils.generateRandomString(10);

        String sql = "CREATE DATABASE "+dbUniqueName+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.categories FROM dprince_db.dbo.categories WHERE categories.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.church_contributions FROM dprince_db.dbo.church_contributions WHERE church_contributions.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.church_events FROM dprince_db.dbo.church_events WHERE church_events.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.file_tokens FROM dprince_db.dbo.file_tokens WHERE file_tokens.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.group_members FROM dprince_db.dbo.group_members WHERE group_members.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.institution_communications FROM dprince_db.dbo.institution_communications WHERE institution_communications.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.institution_credit_accounts FROM dprince_db.dbo.institution_credit_accounts WHERE institution_credit_accounts.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.institution_donations FROM dprince_db.dbo.institution_donations WHERE institution_donations.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.institution_families FROM dprince_db.dbo.institution_families WHERE institution_families.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.institution_families_members FROM dprince_db.dbo.institution_families_members WHERE institution_families_members.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.institution_groups FROM dprince_db.dbo.institution_groups WHERE institution_groups.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.institution_members FROM dprince_db.dbo.institution_members WHERE institution_members.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.institution_subscription_renewals FROM dprince_db.dbo.institution_subscription_renewals WHERE institution_subscription_renewals.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.institution_receipts FROM dprince_db.dbo.institution_receipts WHERE institution_receipts.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.member_activities FROM dprince_db.dbo.member_activities WHERE member_activities.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.notifications FROM dprince_db.dbo.notifications WHERE notifications.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.people_to_categories FROM dprince_db.dbo.people_to_categories WHERE people_to_categories.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.priest_signatures FROM dprince_db.dbo.priest_signatures WHERE priest_signatures.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.templates FROM dprince_db.dbo.templates WHERE templates.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.sms_acknowledger FROM dprince_db.dbo.sms_acknowledger WHERE sms_acknowledger.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.subscription_plans FROM dprince_db.dbo.subscription_plans WHERE subscription_plans.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.users FROM dprince_db.dbo.users WHERE users.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.visiting_members FROM dprince_db.dbo.visiting_members WHERE visiting_members.institution_id="+this.getInstitution().getId()+";" +
                "SELECT * INTO "+dbUniqueName+".dbo.institutions FROM dprince_db.dbo.institutions WHERE institutions.id="+this.getInstitution().getId()+";" +

                // Extra
                "SELECT * INTO "+dbUniqueName+".dbo.pincodes FROM dprince_db.dbo.pincodes;" +
                "SELECT * INTO "+dbUniqueName+".dbo.otps FROM dprince_db.dbo.otps where otps.id<0;" +
                "SELECT * INTO "+dbUniqueName+".dbo.login_tokens FROM dprince_db.dbo.login_tokens where login_tokens.id<0;" +
                "SELECT * INTO "+dbUniqueName+".dbo.login_details FROM dprince_db.dbo.login_details where login_details.id<0;";

        try (Connection connection = this.getDataSource().getConnection();
             Statement statement = connection.createStatement()){
            statement.execute(sql);

            this.backupDbFile(dbUniqueName, institution);
        } catch (Exception exception){
            if(this.appRepository!=null) {
                User institutionAdmin = this.appRepository.getUsersRepository()
                        .findByUsername(this.getInstitution().getEmail());
                if (institutionAdmin != null) {
                    Notification notification = Notification.createNotification(
                            institutionAdmin,
                            "Auto Backup failed",
                            institution.getName() + " backup has failed.",
                            NotificationActionType.DELETE,
                            institutionAdmin.getId()
                    );
                    this.getAppRepository().getNotificationsRepository()
                            .save(notification);
                }
            }
        }
    }
}
