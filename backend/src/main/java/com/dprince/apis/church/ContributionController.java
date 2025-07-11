package com.dprince.apis.church;

import com.dprince.apis.church.models.ContributionMaker;
import com.dprince.apis.institution.models.donations.DonationContribution;
import com.dprince.apis.institution.models.donations.parts.SingleDonation;
import com.dprince.apis.institution.validators.ChurchContributionValidator;
import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.configuration.sms.SmsService;
import com.dprince.entities.*;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/contribution")
public class ContributionController {
    private static final Logger log = LoggerFactory.getLogger(ContributionController.class);
    private final AppRepository appRepository;
    private final ObjectMapper objectMapper;
    private final SmsService smsService;



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,


            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @GetMapping("/get/{institutionId}")
    public List<ChurchContribution> getAllContributions(@PathVariable("institutionId") Long institutionId){
        return this.appRepository.getChurchContributionRepository()
                .findAllByInstitutionId(institutionId);
    }



    @Authenticated(userTypes = {
            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @GetMapping("/get")
    public List<ChurchContribution> getAllContributions(@NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        return this.getAllContributions(loggedInUser.getInstitutionId());
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/save")
    public List<ChurchContribution> saveContributions(@RequestBody @Valid ContributionMaker contributionMaker,
                                                      @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        loggedInUser.checkUserFulfillsRequirements(contributionMaker, this.appRepository);

        contributionMaker.getContributions().parallelStream().forEach(contribution->{
            contribution.setInstitutionId(contributionMaker.getInstitutionId());
        });
        Iterable<ChurchContribution> savedContributions = this.appRepository
                .getChurchContributionRepository()
                .saveAll(contributionMaker.getContributions());
        List<ChurchContribution> output = new ArrayList<>();
        savedContributions.forEach(output::add);

        String notificationTitle = "Contribution Created";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                "Contributions",
                NotificationActionType.CREATE,
                contributionMaker.getInstitutionId()
        );
        this.appRepository.getNotificationsRepository().save(notification);
        return output;
    }



    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR,

            UserType.CHURCH_ADMINISTRATOR,
            UserType.CHURCH_ASSISTANT_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ADMINISTRATOR,
            UserType.CHURCH_BRANCH_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/contribute")
    public ApiWorkFeedback contribute(@RequestBody @Validated({ChurchContributionValidator.class}) DonationContribution contributor,
                                      @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        // Date today = DateUtils.atTheBeginningOfTheDay(DateUtils.getNowDateTime());
        InstitutionMember member = this.appRepository.getInstitutionMemberRepository()
                .findById(contributor.getMemberId()).orElse(null);
        if(member==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Church member doe not exist.");
        }

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(member.getInstitutionId())
                .orElseThrow(()->{
                    // Throw error kuberako ko iyi institution itabaho
                    return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                            "The Church does not exist.");
                });

        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            boolean allowed;
            if(User.getMainChurchAdministrators().contains(loggedInUser.getUserType())
                    && institution.getParentInstitutionId()!=null){
                allowed = (institution.getParentInstitutionId().equals(loggedInUser.getInstitutionId()));
            } else {
                allowed = member.getInstitutionId().equals(loggedInUser.getInstitutionId());
            }
            if(!allowed){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "You have no right to perform this operation.");
            }
        }


        if(contributor.getCreditAccount()!=null) {
            List<Long> institutionIds = new ArrayList<>();
            institutionIds.add(institution.getId());
            if(institution.getParentInstitutionId()!=null) institutionIds.add(institution.getParentInstitutionId());
            InstitutionCreditAccount creditAccount = this.appRepository
                    .getCreditAccountsRepository()
                    .findByInstitutionIdInAndId(institutionIds,
                            contributor.getCreditAccount())
                    .orElseThrow(() -> {
                        // Throw an error because the church contribution does not exist in this institution
                        return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                "No such Credit account found");
                    });
        }


        InstitutionReceipt receipt = new InstitutionReceipt();
        receipt.setInstitutionId(member.getInstitutionId());
        String receiptCode = institution.generateReceiptNo(this.appRepository, null);
        receipt.setReceiptNo(receiptCode);
        receipt.setMemberId(member.getId());
        receipt.setPaymentMode(contributor.getPaymentMode());
        receipt.setEntryDate(contributor.getEntryDate());
        receipt.setCreditAccountId(contributor.getCreditAccount());



        receipt.setRemarks(contributor.getRemarks());
        receipt.setReferenceAccount(contributor.getReferenceAccount());
        receipt.setBankReference(contributor.getBankReference());
        receipt.setReferenceNo(contributor.getReferenceNo());

        boolean contributionDone = false;
        InstitutionReceipt savedReceipt = receipt.save(this.appRepository, institution);
        if(savedReceipt!=null) {
            List<InstitutionDonation> contributions = new ArrayList<>();
            for (SingleDonation singleContribution : contributor.getDonations()) {
                ChurchContribution savedContribution = this.appRepository
                        .getChurchContributionRepository()
                        .findById(singleContribution.getContributionId())
                        .orElse(null);
                if (savedContribution == null) {
                    break;
                } else {
                    InstitutionDonation donation = new InstitutionDonation();
                    donation.setReceiptNo(receipt.getReceiptNo());
                    donation.setInstitutionId(member.getInstitutionId());
                    donation.setMemberId(member.getId());
                    donation.setChurchContributionId(savedContribution.getId());
                    donation.setAmount(singleContribution.getAmount());
                    donation.setEntryDate(contributor.getEntryDate());
                    InstitutionDonation savedDonation = this.appRepository
                            .getInstitutionDonationRepository()
                            .save(donation);
                    contributions.add(savedDonation);
                    member.addContribution(savedContribution.getId());
                }
            }

            contributionDone = contributions.size() == contributor.getDonations().size();
            if (!contributionDone) receipt.delete(appRepository);

            savedReceipt.setDonations(contributions);
            savedReceipt.setMember(member);
            savedReceipt.setObjectMapper(this.objectMapper);
            savedReceipt.addInstitution(this.appRepository);

            this.appRepository.getInstitutionMemberRepository().save(member);
            // member.subscribe(this.appRepository, institution, contributor);
        }

        String notificationTitle = contributionDone ? "Contributed" : "Failed to Contribute";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                member.getCode(),
                contributionDone ? NotificationActionType.CREATE : NotificationActionType.DELETE,
                member.getInstitutionId()
        );
        this.appRepository.getNotificationsRepository().save(notification);

        return ApiWorkFeedback.builder()
                .message(contributionDone ? "Saved." : "Failed to Save")
                .successful(contributionDone)
                .object(contributionDone ? savedReceipt : null)
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
    @DeleteMapping("/delete/{contributionId}")
    public ApiWorkFeedback deleteContribution(@PathVariable("contributionId") Long contributionId,
                                              @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);

        ChurchContribution contribution = this.appRepository
                .getChurchContributionRepository()
                .findById(contributionId).orElse(null);
        if(contribution==null){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "The contribution you are trying to delete does not exist.");
        }
        if(!User.noOrganizationUsers().contains(loggedInUser.getUserType())){
            if(!loggedInUser.getInstitutionId().equals(contribution.getInstitutionId())){
                throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                        "You do not have the right to delete this contribution.");
            }
        }

        this.appRepository.getChurchContributionRepository().deleteById(contributionId);

        String notificationTitle = "Deleted Contribution";
        Notification notification = Notification.createNotification(
                loggedInUser,
                notificationTitle,
                contribution.getName(),
                NotificationActionType.CREATE,
                contribution.getInstitutionId()
        );
        this.appRepository.getNotificationsRepository().save(notification);
        return ApiWorkFeedback.builder()
                .message("Deleted.")
                .successful(true)
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
    @GetMapping("/get-receipt/{receiptNo}")
    public ApiWorkFeedback getContribution(@PathVariable("receiptNo") String receiptNo,
                                             @NonNull HttpSession session){
        User loggedInUser = User.getUserFromSession(session);
        InstitutionReceipt receipt = this.appRepository
                .getInstitutionReceiptsRepository()
                .findByReceiptNo(receiptNo)
                .orElse(null);
        if(receipt==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Receipt Not found.");
        receipt.setObjectMapper(this.objectMapper);
        receipt.populate(appRepository, true);
        return ApiWorkFeedback.builder()
                .message("Loaded.")
                .object(receipt)
                .successful(true)
                .build();
    }
}
