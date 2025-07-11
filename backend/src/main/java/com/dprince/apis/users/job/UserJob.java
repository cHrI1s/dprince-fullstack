package com.dprince.apis.users.job;

import com.dprince.entities.utils.AppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * * @author Chris Ndayishimiye
 * * @created 11/16/23
 */
@Component
public class UserJob {
    private final AppRepository appRepository;

    @Autowired
    public UserJob(AppRepository appRepository){
        this.appRepository = appRepository;
    }

    /*
    // Every day at 5pm
    @Scheduled(cron="0 17 * * *")
    public void sendNotification(){
        List<User> allUsers = this.appRepository.getTeachersRepository().findAll();
        allUsers.forEach(singleTeacher->{
            List<Subject> todaySubjects = singleTeacher.getTodaySubjects(this.appRepository);

            todaySubjects.forEach(singleSubject->{
                // Get marked attendance for the hour
                // List<StudentAttendanceModel>
            });
        });
    }
     */
}
