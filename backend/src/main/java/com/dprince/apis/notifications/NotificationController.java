package com.dprince.apis.notifications;

import com.dprince.apis.utils.DateUtils;
import com.dprince.apis.utils.models.ListQuery;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.entities.InstitutionMember;
import com.dprince.entities.Notification;
import com.dprince.entities.User;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import com.dprince.entities.vos.PageVO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/notification")
public class NotificationController {
    private final AppRepository appRepository;

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
    public PageVO getNotifications(@RequestBody @Valid ListQuery listQuery,
                                   @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        Date lastWeekDate = DateUtils.addDays(-7);

        int page = listQuery.getPage()-1;
        if(page<0) page = 0;
        PageRequest pageRequest = PageRequest.of(
                page,
                listQuery.getSize(),
                Sort.by(Sort.Direction.DESC, Notification.Fields.notificationTime)
        );

        Page<Notification> notificationPage;
        if(User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            List<UserType> superAdministratorsTypes = User.noOrganizationUsers();
            List<Long> extraSuperAdminsIds = this.appRepository.getUsersRepository()
                    .findAllByUserTypeIn(superAdministratorsTypes)
                    .stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            notificationPage = this.appRepository.getNotificationsRepository()
                    .getAllVisibleToSuperAdmins(extraSuperAdminsIds, lastWeekDate, pageRequest);
        } else {
            notificationPage = this.appRepository.getNotificationsRepository()
                    .findAllByInstitutionIdAndNotificationTimeGreaterThan(
                            User.noOrganizationUsers().contains(loggedInUser.getUserType())
                                    ? null
                                    : loggedInUser.getInstitutionId(),
                            lastWeekDate,
                            pageRequest);
        }

        notificationPage.getContent().forEach(notification -> {
            if(notification.getExecutor()!=null) {
                InstitutionMember member = this.appRepository
                        .getInstitutionMemberRepository()
                        .findById(notification.getExecutor())
                        .orElse(null);
                if (member != null) {
                    notification.setExecutorFullName(member.getFullName());
                } else {
                    notification.setExecutorFullName("System");
                }
            } else {
                notification.setExecutorFullName("System");
            }
        });
        return PageVO.getPageVO(notificationPage);
    }
}
