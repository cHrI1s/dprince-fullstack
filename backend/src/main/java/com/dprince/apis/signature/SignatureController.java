package com.dprince.apis.signature;

import com.dprince.apis.institution.models.PriestSignatureRequestModel;
import com.dprince.apis.institution.models.validations.DefaultSignatureSetter;
import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.configuration.files.LocalFileStorageService;
import com.dprince.configuration.files.utils.ApplicationFileType;
import com.dprince.entities.*;
import com.dprince.entities.enums.ChurchFunction;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/signature")
public class SignatureController {
    private final AppRepository appRepository;
    private final LocalFileStorageService storageService;


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.ORGANIZATION_ADMINISTRATOR
    })
    @PostMapping("/save")
    public ApiWorkFeedback saveSignature(@RequestBody @Valid PriestSignature priestSignature,
                                         @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(priestSignature, this.appRepository);

        InstitutionMember savedMember = this.appRepository.getInstitutionMemberRepository()
                .findById(priestSignature.getMemberId()).orElse(null);
        if(savedMember==null) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Member not found.");
        }

        // Ensure one signature is there.
        PriestSignature savedSignature = this.appRepository.getPriestSignatureRepository()
                .findByInstitutionIdAndMemberId(priestSignature.getInstitutionId(), priestSignature.getMemberId());
        if(savedSignature!=null) priestSignature.setId(savedSignature.getId());
        savedSignature = this.appRepository.getPriestSignatureRepository().save(priestSignature);

        String notificationTitle = "Signature Saved";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                null,
                NotificationActionType.UPDATE,
                savedSignature.getInstitutionId()
        );
        this.appRepository.getNotificationsRepository().save(notification);
        return ApiWorkFeedback
                .builder()
                .message("Signature saved.")
                .object(savedSignature)
                .successful(true)
                .build();
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
    })
    @PostMapping("/get")
    public ApiWorkFeedback getSingleInstitution(@RequestBody @Validated PriestSignatureRequestModel priestSignatureRequestModel,
                                                @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(priestSignatureRequestModel, this.appRepository);

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(priestSignatureRequestModel.getInstitutionId())
                .orElseThrow(()->{
                    // Thrown an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Church not recognized!");
                });
        if(StringUtils.isEmpty(institution.getPriestSignature())){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "No signature set for this Church!");
        }

        return  ApiWorkFeedback
                .builder()
                .successful(true)
                .object(institution)
                .message("Loaded!")
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
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/set-default")
    public ApiWorkFeedback setChurchDefaultSignature(@RequestBody @Validated({DefaultSignatureSetter.class}) PriestSignatureRequestModel priestSignatureRequestModel,
                                                     @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(priestSignatureRequestModel, this.appRepository);
        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(priestSignatureRequestModel.getInstitutionId())
                .orElseThrow(()->{
                    // Thrown an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Institution not recognized!");
                });
        PriestSignature priestSignature = this.appRepository.getPriestSignatureRepository()
                .findByInstitutionIdAndMemberId(institution.getId(), priestSignatureRequestModel.getPriestId());
        String message = "No Signature found for this Priest!";
        boolean successful = false;
        if(priestSignature!=null){
            institution.setPriestSignature(priestSignature.getSignatureImage());
            institution = this.appRepository.getInstitutionRepository().save(institution);
            message = "Signature saved!";
            successful = true;

            final String oldSignature = institution.getPriestSignature();
            CompletableFuture.runAsync(()-> storageService.delete(oldSignature, ApplicationFileType.FILE));
        }

        return ApiWorkFeedback.builder()
                .message(message)
                .object(successful ? institution : null)
                .successful(successful)
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
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/list")
    public ApiWorkFeedback getAllSignatures(@RequestBody @Validated PriestSignatureRequestModel requestModel,
                                                     @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(requestModel, this.appRepository);
        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(requestModel.getInstitutionId())
                .orElseThrow(()->{
                    // Thrown an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Institution not recognized!");
                });
        List<InstitutionMember> priests = this.appRepository
                .getInstitutionMemberRepository().getPriests(institution.getId(), ChurchFunction.PRIEST);
        List<Long> priestIds = priests.parallelStream()
                .map(InstitutionMember::getId)
                .collect(Collectors.toList());
        Map<Long, String> priestSignatures = this.appRepository.getPriestSignatureRepository()
                .findAllByMemberIdIn(priestIds)
                .parallelStream()
                .collect(Collectors.toMap(PriestSignature::getMemberId, PriestSignature::getSignatureImage));

        priests.parallelStream()
                .forEach(priest->{
                    String signature = priestSignatures.getOrDefault(priest.getId(), null);
                    priest.setSignature(signature);
                });

        String message = "Loaded!";
        boolean successful = false;

        return ApiWorkFeedback.builder()
                .message(message)
                .objects(priests)
                .successful(successful)
                .build();
    }
}
