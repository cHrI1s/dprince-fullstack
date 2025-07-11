package com.dprince.apis.events;


import com.dprince.apis.events.models.Attendance;
import com.dprince.apis.events.models.ConcludeEventModel;
import com.dprince.apis.events.models.EventRequestor;
import com.dprince.apis.events.models.FinalizeEventModel;
import com.dprince.apis.events.models.validators.ChurchEventCreationValidator;
import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.apis.utils.models.ChurchBelongingListQuery;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.configuration.files.LocalFileStorageService;
import com.dprince.entities.ChurchEvent;
import com.dprince.entities.Institution;
import com.dprince.entities.Notification;
import com.dprince.entities.User;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import com.dprince.entities.vos.PageVO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/event")
public class EventsController {
    private static final Logger log = LoggerFactory.getLogger(EventsController.class);

    private final EntityManagerFactory entityManagerFactory;
        public EntityManager getEntityManager() {
            return this.entityManagerFactory.createEntityManager();
        }

    private final AppRepository repository;
    private final LocalFileStorageService storageService;

    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/save")
    public ApiWorkFeedback save(@RequestBody @Validated({ChurchEventCreationValidator.class}) ChurchEvent eventCreator,
                                @NonNull HttpSession session) {
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(eventCreator, this.repository);

        ChurchEvent churchEvent = new ChurchEvent();
        if (eventCreator.getId() != null) {
            // This means we are performing an update
            churchEvent = this.repository.getChurchEventRepository()
                    .findById(eventCreator.getId())
                    .orElse(null);
            if (churchEvent == null) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "The event you are trying to edit does not exist.");
            }
        }

        // If the user is Super Administrator the
        churchEvent.setTitle(eventCreator.getTitle());
        churchEvent.setInstitutionId(eventCreator.getInstitutionId());
        churchEvent.setDescription(eventCreator.getDescription());
        churchEvent.setEventDate(eventCreator.getEventDate());
        churchEvent.setExpectedAttendees(eventCreator.getExpectedAttendees());
        churchEvent.setNumberOfAttendees(eventCreator.getNumberOfAttendees());
        churchEvent.setRemarks(eventCreator.getRemarks());
        churchEvent.setPlace(eventCreator.getPlace());
        ChurchEvent savedEvent = this.repository.getChurchEventRepository().save(churchEvent);

        String notificationTitle = eventCreator.getId() != null
                ? "Event Updated"
                : "Event Created";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                savedEvent.getTitle(),
                eventCreator.getId() != null ? NotificationActionType.UPDATE : NotificationActionType.CREATE,
                savedEvent.getInstitutionId()
        );
        String message = (eventCreator.getId() != null) ? "Event Updated." : "Event Saved.";
        this.repository.getNotificationsRepository().save(notification);

        return ApiWorkFeedback.builder()
                .message(message)
                .successful(true)
                .object(savedEvent)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/share-event")
    public ApiWorkFeedback shareEvent(@RequestBody @Valid FinalizeEventModel finalizeEventModel,
                                      @NonNull HttpSession session) {
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(finalizeEventModel, this.repository);

        ChurchEvent churchEvent = this.repository.getChurchEventRepository()
                .findById(finalizeEventModel.getId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "We failed to load institution data"));


        ChurchEvent save =  this.repository.getChurchEventRepository()
               .save(churchEvent);
        return ApiWorkFeedback
                .builder()
                .message("Message sent")
                .object(save)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/mark-attendance")
    public ApiWorkFeedback markAttendance(@RequestBody Attendance attendance,
                                          @Valid @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(attendance, this.repository);
        attendance.fetchEvent(this.repository);
        if(attendance.getEvent()==null) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "No such event found.");
        }
        attendance.doImport(this.repository, this.storageService, loggedInUser);

        return ApiWorkFeedback
                .builder()
                .successful(true)
                .message("Importing partners. This may take a while. You will be notified of the status of import")
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/conclude-event")
    public ApiWorkFeedback concludeEvent(@RequestBody ConcludeEventModel concludeEventModel){
        ChurchEvent churchEvent = this.repository.getChurchEventRepository()
                .findById(concludeEventModel.getEventId()).orElse(null);
        if(churchEvent==null){
            throw  new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "The Event does not exist.");
        }
        churchEvent.setSettled(true);
        ChurchEvent saveEvent = this.repository.getChurchEventRepository().save(churchEvent);
        return ApiWorkFeedback
                .builder()
                .message("Event Settled successfully")
                .object(saveEvent)
                .build();
    }




    /*@Authenticated(userTypes = {

            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/update")
    public ApiWorkFeedback update(@RequestBody @Validated({ChurchEventUpdateValidator.class}) ChurchEventCreator eventCreator,
                                  @NonNull HttpSession session) {
        return this.save(eventCreator, session);
    }*/


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @DeleteMapping("/delete/{churchEventId}")
    public ApiWorkFeedback delete(@PathVariable("churchEventId") Long churchEventId,
                                  @NonNull HttpSession session) {
        User loggedInUser = User.getUserFromSession(session);
        ChurchEvent savedEvent = this.repository
                .getChurchEventRepository()
                .findById(churchEventId)
                .orElse(null);
        if (savedEvent == null) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "No such Event found.");
        }

        if (!User.noOrganizationUsers().contains(loggedInUser.getUserType())) {
            if (!savedEvent.getInstitutionId().equals(loggedInUser.getInstitutionId())) {
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "You are not allowed to delete this event.");
            }
        }
        this.repository.getChurchEventRepository().deleteById(churchEventId);


        String notificationTitle = "Event Deleted";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                savedEvent.getTitle(),
                NotificationActionType.DELETE,
                savedEvent.getInstitutionId()
        );
        this.repository.getNotificationsRepository().save(notification);

        return ApiWorkFeedback.builder()
                .successful(true)
                .message("Event deleted.")
                .build();
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/list")
    public PageVO getAllListEvent(@RequestBody ChurchBelongingListQuery listQuery,
                                  @NonNull HttpSession session) {
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(listQuery, this.repository);

        int pageNumber = listQuery.getPage() - 1;
        PageRequest pageRequest = PageRequest.of(
                pageNumber,
                listQuery.getSize(),
                Sort.by(Sort.Direction.DESC, ChurchEvent.Fields.eventDate)
        );

        List<Institution> childrenInstitutions;
        Institution institution = this.repository.getInstitutionRepository()
                .findById(listQuery.getInstitutionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Church not recognized!"));


        Page<ChurchEvent> page = null;
        if (institution.getParentInstitutionId() == null) {
            childrenInstitutions = this.repository.getInstitutionRepository()
                    .findAllByParentInstitutionId(listQuery.getInstitutionId());


            // La pere arabe nivyumana na rwiwe
            List<Long> idsOfAllowedChurches = childrenInstitutions.stream()
                    .map(Institution::getId)
                    .collect(Collectors.toList());
            idsOfAllowedChurches.add(listQuery.getInstitutionId());


            // Noronze ivyanje nivyabana
            page = this.repository.getChurchEventRepository()
                    .findAllByInstitutionIdIn(idsOfAllowedChurches, pageRequest);
            page.getContent().parallelStream().forEach(churchEvent -> {
                Institution eventOwner = null;
                for(Institution singleInstitution : childrenInstitutions){
                    if(singleInstitution.getId().equals(churchEvent.getInstitutionId())){
                        eventOwner = singleInstitution;
                        break;
                    }
                }

                if(eventOwner!=null) churchEvent.setChildInstitution(eventOwner);
            });
        } else {
            // Ni umwana araba rwiwe
            page = this.repository.getChurchEventRepository()
                    .findAllByInstitutionId(listQuery.getInstitutionId(), pageRequest);
        }
        return PageVO.getPageVO(page);
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/search")
    public PageVO getAllEvents(@RequestBody ChurchBelongingListQuery listQuery,
                                  @NonNull HttpSession session) {
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(listQuery, this.repository);

        List<Long> institutionsIds = new ArrayList<>();
        Institution institution = this.repository.getInstitutionRepository()
                .findById(listQuery.getInstitutionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "Church not recognized!"));
        institutionsIds.add(institution.getId());

        List<Long> childrenInstitutionsIds = this.repository.getInstitutionRepository()
                .findAllByParentInstitutionId(listQuery.getInstitutionId())
                .parallelStream()
                .map(Institution::getId).collect(Collectors.toList());
        institutionsIds.addAll(childrenInstitutionsIds);

        listQuery.setInstitutionsIds(institutionsIds);
        Page<ChurchEvent> foundPage = ChurchEvent.search(this.getEntityManager(), this.repository, listQuery);

        return PageVO.getPageVO(foundPage);
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/get-event")
    public ApiWorkFeedback getAllEvents(@RequestBody EventRequestor requestor,
                                  @NonNull HttpSession session) {
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(requestor, this.repository);

        ChurchEvent churchEvent = this.repository.getChurchEventRepository()
                        .findById(requestor.getEventId())
                                .orElseThrow(()->{
                                    // throw an error
                                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                            "Event not recognized!");
                                });
        churchEvent.populate(this.repository);
        return ApiWorkFeedback.builder()
                .message("Event loaded!")
                .object(churchEvent)
                .successful(true)
                .build();
    }
}
