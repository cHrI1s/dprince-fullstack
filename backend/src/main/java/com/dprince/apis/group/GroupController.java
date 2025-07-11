package com.dprince.apis.group;

import com.dprince.apis.group.models.GroupCreator;
import com.dprince.apis.group.models.GroupMemberAddModel;
import com.dprince.apis.group.models.GroupMemberDeletionModel;
import com.dprince.apis.institution.models.AdminListingQuery;
import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.apis.utils.models.SimpleGetModel;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.entities.*;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;


@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/group/")
public class GroupController {

    private static final Logger log = LoggerFactory.getLogger(GroupController.class);
    private final AppRepository appRepository;

    private final EntityManagerFactory entityManagerFactory;
        private EntityManager getEntityManager() {
            return this.entityManagerFactory.createEntityManager();
        }

    private final ObjectMapper objectMapper;
    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/save")
    public ApiWorkFeedback saveGroup(@RequestBody @Valid GroupCreator groupCreator,
                                     @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(groupCreator, this.appRepository);

        groupCreator.getGroups().forEach(singleGroup->{
            singleGroup.setInstitutionId(groupCreator.getInstitutionId());
        });
        List<InstitutionGroup> savedGroup = this.appRepository.getGroupRepository().saveAll(groupCreator.getGroups());

        String notificationTitle = "Group(s) Created";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                null,
                NotificationActionType.CREATE,
                groupCreator.getInstitutionId()
        );
        this.appRepository.getNotificationsRepository().save(notification);
        return ApiWorkFeedback.builder()
                .message("Saved.")
                .successful(true)
                .objects(savedGroup)
                .build();
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/update")
    public ApiWorkFeedback update(@RequestBody @Valid InstitutionGroup groupUpdateModel,
                                  @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        if(groupUpdateModel.getId()==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "The group id is missing.");
        }
        InstitutionGroup institutionGroup = this.appRepository.getGroupRepository()
                .save(groupUpdateModel);

        String notificationTitle = "Group Updated";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                null,
                NotificationActionType.UPDATE,
                institutionGroup.getInstitutionId()
        );
        this.appRepository.getNotificationsRepository().save(notification);
        return ApiWorkFeedback
                .builder()
                .successful(true)
                .object(institutionGroup)
                .message("Updated")
                .build();

    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/get")
    public List<InstitutionGroup> save(@RequestBody @Valid SimpleGetModel model,
                                       @NonNull HttpSession session) {
        User loggedInUser= User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(model, this.appRepository);
        return this.appRepository.getGroupRepository()
                .findAllByInstitutionId(model.getInstitutionId());
    }




    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @DeleteMapping("/delete/{id}")
    public ApiWorkFeedback save(@PathVariable("id") Long id,
                                @NonNull HttpSession session) {
        User loggedInUser = User.getUserFromSession(session);
        InstitutionGroup groupToDelete = this.appRepository
                .getGroupRepository()
                .findById(id)
                .orElse(null);
        if(groupToDelete==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "No such group found.");
        }

        long foundMembers = this.appRepository.getGroupMemberRepository()
                .countAllByGroupId(groupToDelete.getId());
        if(foundMembers>0){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "The group is already in use.");
        }
        this.appRepository.getGroupRepository().deleteById(id);


        String notificationTitle = "Group Deleted";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                null,
                NotificationActionType.DELETE,
                groupToDelete.getInstitutionId()
        );
        this.appRepository.getNotificationsRepository().save(notification);
        return ApiWorkFeedback.builder()
                .successful(true)
                .message("Deleted.")
                .build();
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/delete-member")
    public ApiWorkFeedback deleteMemberFromGroup(@RequestBody @Valid GroupMemberDeletionModel
                                                             groupMemberDeletionModel,
                                                 @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        InstitutionGroup memberToDelete = this.appRepository.getGroupRepository()
                .findById(groupMemberDeletionModel.getGroupId()).orElse(null);
        if(memberToDelete==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "No such group found.");
        }

        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            if(!memberToDelete.getInstitutionId().equals(loggedInUser.getInstitutionId())){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "You do not have the right to delete this user from this group.");
            }
        }

        this.appRepository.getGroupMemberRepository()
                .deleteByMemberIdAndGroupId(
                        groupMemberDeletionModel.getMemberId(),
                        groupMemberDeletionModel.getGroupId());
        return ApiWorkFeedback.builder()
                .successful(true)
                .message("Deleted.")
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/add-member")
    public ApiWorkFeedback addMember(@RequestBody @Valid GroupMemberAddModel model,
                                     @NonNull HttpSession session) {
        User loggedInUser = User.getUserFromSession(session);
        InstitutionMember member = this.appRepository
                .getInstitutionMemberRepository()
                .findById(model.getMemberId())
                .orElseThrow(()->new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Member not recognized"));

        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            if(!member.getInstitutionId().equals(loggedInUser.getInstitutionId())){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "You do not have the right to perform this action.");
            }
        }
        this.appRepository.getGroupMemberRepository()
                .deleteAllByMemberId(member.getId());
        model.getGroupsIds().parallelStream()
                .forEach(groupId->{
                    this.appRepository.getGroupRepository()
                            .findById(groupId)
                            .ifPresent(group->{
                                GroupMember savedMember = this.appRepository.getGroupMemberRepository()
                                        .findAllByGroupIdAndMemberId(groupId, member.getId())
                                        .orElse(null);
                                if(savedMember==null) {
                                    // Save only and only if the member is not present in the group
                                    GroupMember groupMember = new GroupMember();
                                    groupMember.setMemberId(member.getId());
                                    groupMember.setGroupId(groupId);
                                    this.appRepository.getGroupMemberRepository().save(groupMember);
                                }
                            });
                });

        return ApiWorkFeedback.builder()
                .message("Saved!")
                .successful(true)
                .build();
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/list-members")
    public Page<InstitutionMember> listMembers(@RequestBody @Valid AdminListingQuery listQuery,
                                               @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(listQuery, this.appRepository);

        return GroupMember.listMembers(this.getEntityManager(),
                this.appRepository,
                listQuery);
    }
}
