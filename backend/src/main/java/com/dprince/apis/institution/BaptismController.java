package com.dprince.apis.institution;


import com.dprince.apis.institution.models.BaptismModel;
import com.dprince.apis.institution.models.InstitutionMemberSearchModel;
import com.dprince.apis.institution.models.validations.GuestPastorBaptist;
import com.dprince.apis.institution.models.validations.LocalPastorBaptist;
import com.dprince.apis.institution.vos.PriestVO;
import com.dprince.apis.institution.vos.enums.LocalizedPriest;
import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.entities.*;
import com.dprince.entities.enums.BaptismStatus;
import com.dprince.entities.enums.ChurchFunction;
import com.dprince.entities.enums.InstitutionType;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/baptism")
public class BaptismController {
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
    @PostMapping("/create/guest-baptist")
    public ApiWorkFeedback baptizeMember(@RequestBody @Valid GuestPriest guestPriest,
                                         @NonNull HttpSession session) {
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(guestPriest, this.appRepository);

        GuestPriest savedPriest = this.appRepository.getGuestPriestsRepository()
                .save(guestPriest);
        return ApiWorkFeedback.builder()
                .message("Priest Saved!")
                .successful(true)
                .object(savedPriest)
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
    @PostMapping("/get-baptists")
    public Map<LocalizedPriest, List<PriestVO>> searchInstitutionPriest(@RequestBody @Valid InstitutionMemberSearchModel searchModel,
                                                                        @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(searchModel, this.appRepository);

        Map<LocalizedPriest, List<PriestVO>> theMap = new HashMap<>();
        List<PriestVO> localPriests = this.appRepository.getInstitutionMemberRepository()
                .getPriests(searchModel.getInstitutionId(), ChurchFunction.PRIEST)
                .parallelStream()
                .map(priest -> PriestVO.builder()
                        .id(priest.getId())
                        .fullName(priest.getFullName())
                        .build())
                .sorted(Comparator.comparing(PriestVO::getFullName))
                .collect(Collectors.toList());
        theMap.put(LocalizedPriest.LOCAL, localPriests);

        List<PriestVO> guestPriests = this.appRepository.getGuestPriestsRepository()
                .findAllByInstitutionId(searchModel.getInstitutionId())
                .parallelStream()
                .map(priest -> PriestVO.builder()
                        .id(priest.getId())
                        .fullName(priest.getFullName())
                        .build())
                .sorted(Comparator.comparing(PriestVO::getFullName))
                .collect(Collectors.toList());
        theMap.put(LocalizedPriest.GUEST, guestPriests);
        return theMap;
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
    @PostMapping("/local/baptize")
    public ApiWorkFeedback baptizeMember(@RequestBody @Validated({LocalPastorBaptist.class}) BaptismModel baptismModel,
                                         @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(baptismModel, this.appRepository);
        InstitutionMember savedMember = this.appRepository.getInstitutionMemberRepository()
                .findById(baptismModel.getBaptismalCandidateId()).orElse(null);
        if(savedMember==null) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Member not found.");
        }
        Institution institution = this.appRepository
                .getInstitutionRepository()
                .findById(savedMember.getInstitutionId())
                .orElseThrow(()->
                        new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                "Member's Church does not exist.")
                );
        if(!savedMember.getInstitutionId().equals(institution.getId())){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Member not recognized in the church.");
        }

        if(!institution.getInstitutionType().equals(InstitutionType.CHURCH)){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Only Church Members are allowed to get baptized.");
        }

        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            if(!loggedInUser.getInstitutionId().equals(savedMember.getInstitutionId())){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "You are only allowed to set within your institution.");
            }
        }
        if(baptismModel.isLocalBaptizer()) savedMember.setBaptist(baptismModel.getBaptist());
        else savedMember.setGuestBaptist(baptismModel.getGuestBaptist());
        savedMember.setBaptized(BaptismStatus.BAPTIZED);
        savedMember.setDateOfBaptism(baptismModel.getDateOfBaptism());
        this.appRepository.getInstitutionMemberRepository().save(savedMember);

        MemberActivity.saveActivity(this.appRepository,
                savedMember.getId(),
                institution.getId());
        return ApiWorkFeedback
                .builder()
                .successful(true)
                .object(savedMember)
                .message("Baptized.")
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
    @PostMapping("/guest/baptize")
    public ApiWorkFeedback memberBaptism(@RequestBody @Validated({GuestPastorBaptist.class}) BaptismModel baptismModel,
                                         @NonNull HttpSession session){
        return this.baptizeMember(baptismModel, session);
    }
}
