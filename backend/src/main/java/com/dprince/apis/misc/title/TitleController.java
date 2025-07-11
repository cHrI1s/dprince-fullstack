package com.dprince.apis.misc.title;


import com.dprince.apis.misc.title.models.TitlesCreator;
import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.entities.Notification;
import com.dprince.entities.PersonTitle;
import com.dprince.entities.User;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import com.dprince.startup.GeneralValues;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/title")
public class TitleController {
    private final AppRepository appRepository;


    @Authenticated(userTypes = {
        UserType.SUPER_ADMINISTRATOR,
        UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/save")
    public ApiWorkFeedback save(@RequestBody @Valid TitlesCreator creator,
                                @NonNull HttpSession session){
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        List<PersonTitle> allTitles = creator.getTitles();
        List<PersonTitle> savedTitles = this.appRepository.getPersonTitlesRepository()
                .saveAll(allTitles);
        GeneralValues.addPersonTitles(savedTitles);

        List<String> titlesString = savedTitles.
                parallelStream()
                .map(PersonTitle::getShortName)
                .collect(Collectors.toList());
        String notificationTitle = creator.isUpdate() ? "Title(s) Updated": "Title(s) Saved";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                "The titles are: "+String.join(", ", titlesString),
                NotificationActionType.CREATE,
                null
        );
        this.appRepository.getNotificationsRepository().save(notification);
        return ApiWorkFeedback.builder()
                .message("Saved")
                .successful(true)
                .objects(savedTitles)
                .build();
    }



    @Authenticated(userTypes = {
        UserType.SUPER_ADMINISTRATOR,
        UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/update")
    public ApiWorkFeedback update(@RequestBody @Valid PersonTitle personTitle,
                                  @NonNull HttpSession session){
        TitlesCreator creator = new TitlesCreator();
        creator.setUpdate(true);
        creator.addTitle(personTitle);
        return this.save(creator, session);
    }



    @Authenticated
    @GetMapping("/get")
    public List<PersonTitle> save(){
        return new ArrayList<>(GeneralValues.PERSON_TITLES.values());
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @DeleteMapping("/delete/{id}")
    public ApiWorkFeedback save(@PathVariable("id") Long id,
                                @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        this.appRepository.getPersonTitlesRepository().deleteById(id);

        String notificationTitle = "Title Deleted";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                null,
                NotificationActionType.CREATE,
                null
        );
        this.appRepository.getNotificationsRepository().save(notification);
        return ApiWorkFeedback.builder()
                .successful(true)
                .message("Deleted.")
                .build();
    }
}
