package com.dprince.apis.certificates;

import com.dprince.apis.certificates.models.BackgroundRequestor;
import com.dprince.apis.certificates.validators.CertificateGeneration;
import com.dprince.apis.certificates.validators.CertificateRetrieval;
import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.entities.*;
import com.dprince.entities.enums.CertificateType;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.models.Certifier;
import com.dprince.entities.utils.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/certificates")
public class CertificateController {
    private final AppRepository appRepository;

    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/generate")
    public ApiWorkFeedback generateCertificate(@RequestBody @Validated({CertificateGeneration.class}) Certificate certificate,
                                               @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(certificate, this.appRepository);

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(certificate.getInstitutionId())
                .orElseThrow(()->{
                    // Throw an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Institution not recognized!");
                });

        Certifier certifier = null;
        switch(certificate.getCertificateType()){
            case BAPTISM:
            case BIRTHDAY:
            case MEMBERSHIP:
            case KID_DEDICATION:
            default:
                InstitutionMember member = this.appRepository.getInstitutionMemberRepository()
                        .findByIdAndInstitutionId(certificate.getOwnerId(), institution.getId())
                        .orElseThrow(()->{
                            // The owner is not recognized
                            return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                    "Owner is not recognized!");
                        });
                certifier = Certifier.builder()
                        .institution(institution)
                        .ownerCode(member.getCode())
                        .build();
                break;

            case MARRIAGE:
                InstitutionFamily family = this.appRepository.getInstitutionFamilyRepository()
                        .findById(certificate.getOwnerId()).orElseThrow(()->{
                            // The owner is not recognized
                            return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                    "Family is not recognized!");
                        });
                if(family.getFamilyCode()==null) {
                    String familyCode = institution.generateFamilyCode(this.appRepository);
                    family.setFamilyCode(familyCode);
                    this.appRepository.getInstitutionFamilyRepository().save(family);
                }
                certifier = Certifier.builder()
                        .institution(institution)
                        .ownerCode(family.getFamilyCode())
                        .build();
                break;
        }

        String message = "Failed to generate Certificate!";
        boolean done = false;
        Certificate savedCertificate = null;
        if(certifier!=null){
            certificate.generateNumber(certifier, this.appRepository);
            savedCertificate = this.appRepository.getCertificatesRepository()
                    .save(certificate);
            savedCertificate.setDoer(loggedInUser.getFullName());

            message = "Certificate Generated!";
            done = true;
        }
        return ApiWorkFeedback.builder()
                .message(message)
                .object(savedCertificate)
                .successful(done)
                .build();
    }


    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/get")
    public ApiWorkFeedback retrieveCertificate(@RequestBody @Validated({CertificateRetrieval.class}) Certificate certificate,
                                               @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(certificate, this.appRepository);

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(certificate.getInstitutionId())
                .orElseThrow(()->{
                    // Throw an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Institution not recognized!");
                });
        InstitutionMember member = this.appRepository.getInstitutionMemberRepository()
                .findByIdAndInstitutionId(certificate.getOwnerId(), institution.getId())
                .orElseThrow(()->{
                    // The owner is not recognized
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Owner is not recognized!");
                });
        member.setInstitution(institution);

        Certifier certifier = Certifier.builder()
                .institution(institution)
                .ownerCode(member.getCode())
                .build();
        certificate.generateNumber(certifier, this.appRepository);
        PageRequest pageRequest = PageRequest.of(0,
                1,
                Sort.by(Sort.Direction.DESC, Certificate.Fields.id));
        List<Certificate> savedCertificates = this.appRepository.getCertificatesRepository()
                .getCertificates(certificate.getCertificateType(),
                        certificate.getOwnerId(),
                        pageRequest);
        String message = "No Certificate Found!";
        boolean successful = false;
        Certificate foundCertificate = null;
        if(!savedCertificates.isEmpty()){
            message = "No certificate Found!";
            foundCertificate = savedCertificates.get(0);
        } else {
            message = "Failed to generate Certificate!";
            foundCertificate = new Certificate();
            foundCertificate.setCertificateType(certificate.getCertificateType());

            foundCertificate.generateNumber(certifier, this.appRepository);
            foundCertificate.setInstitutionId(certificate.getInstitutionId());
            foundCertificate.setDoneBy(loggedInUser.getId());
            foundCertificate.setOwnerId(certificate.getOwnerId());
            foundCertificate = this.appRepository.getCertificatesRepository().save(foundCertificate);
        }

        if(foundCertificate!=null) {
            User userDoer = this.appRepository.getUsersRepository()
                    .findById(foundCertificate.getDoneBy())
                    .orElseThrow(() -> {
                        // Throw an error
                        return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                "Certificate not fully loaded!");
                    });
            foundCertificate.setDoer(userDoer.getFullName());
            message = "Certificate Generated!";
            successful = true;
        }
        return ApiWorkFeedback.builder()
                .message(message)
                .object(foundCertificate)
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
            UserType.CHURCH_DATA_ENTRY_OPERATOR,

            UserType.ORGANIZATION_ADMINISTRATOR,
            UserType.ORGANIZATION_ASSISTANT_ADMINISTRATOR,
            UserType.ORGANIZATION_DATA_ENTRY_OPERATOR,
    })
    @PostMapping("/get-backgrounds")
    public ApiWorkFeedback retrieveBackground(@RequestBody BackgroundRequestor requestor,
                                               @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(requestor, this.appRepository);

        this.appRepository.getInstitutionRepository()
                .findById(requestor.getInstitutionId())
                .orElseThrow(()->{
                    // Throw an error
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "Institution not recognized!");
                });

        List<CertificateBackground> backgrounds = this.appRepository
                .getCertificatesBackgroundsRepository()
                .findAllByInstitutionId(requestor.getInstitutionId());

        Map<CertificateType, CertificateBackground> backgroundMap = backgrounds
                .parallelStream()
                .collect(Collectors.toMap(
                        CertificateBackground::getCertificateType,
                        item->item
                ));
        CertificateType.getAll()
                .parallelStream()
                .forEach(certificateType -> {
                    if(!backgroundMap.containsKey(certificateType)) backgroundMap.put(certificateType, null);
                });
        return ApiWorkFeedback.builder()
                .message("Certificates Background Data Loaded")
                .object(backgroundMap)
                .successful(true)
                .build();
    }
}
