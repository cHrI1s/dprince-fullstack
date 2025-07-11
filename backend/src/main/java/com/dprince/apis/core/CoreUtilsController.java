package com.dprince.apis.core;


import com.dprince.apis.utils.ApiWorkFeedback;
import com.dprince.apis.utils.DateUtils;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.entities.Institution;
import com.dprince.entities.InstitutionDonation;
import com.dprince.entities.InstitutionReceipt;
import com.dprince.entities.PeopleSubscription;
import com.dprince.entities.enums.InstitutionType;
import com.dprince.entities.enums.Subscription;
import com.dprince.entities.enums.UserType;
import com.dprince.entities.utils.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@CrossOrigin
@RestController
@RequestMapping("/core")
public class CoreUtilsController {
    private final AppRepository appRepository;

    @Authenticated(userTypes = {
            UserType.SUPER_ADMINISTRATOR,
            UserType.SUPER_ASSISTANT_ADMINISTRATOR
    })
    @PostMapping("/correct-data")
    public ApiWorkFeedback correctData(){
        CompletableFuture.runAsync(()->{
            // Do the stuff that takes in charge everything related to autocorrection.
            this.appRepository.getInstitutionRepository()
                    .findAll()
                    .parallelStream()
                    .forEach(institution->{
                        // Correct the members corrections
                        institution.autoCorrectData(this.appRepository);
                    });
        });

        return ApiWorkFeedback.builder()
                .message("The process is going on. We will let you know once it is over.")
                .successful(true)
                .build();
    }


    @Authenticated(userTypes = { UserType.SUPER_ADMINISTRATOR })
    @GetMapping("/correct-deadlines")
    public ApiWorkFeedback correctDeadlines(){
        CompletableFuture.runAsync(()->{
            this.appRepository.getPeopleSubscriptionRepository()
                    .deleteAll();
            List<Long> generalMembersIds = this.appRepository
                    .getInstitutionRepository()
                    .findAllByInstitutionType(InstitutionType.GENERAL)
                    .parallelStream()
                    .map(Institution::getId)
                    .collect(Collectors.toList());
            this.appRepository
                    .getInstitutionMemberRepository()
                    .findAllByInstitutionIdIn(generalMembersIds)
                    .parallelStream()
                    .forEach(member->{
                        member.adjustDeadline(false);
                        this.appRepository.getInstitutionMemberRepository().save(member);
                    });
        });

        this.correctPayments();

        return ApiWorkFeedback.builder()
                .message("The process is going on. We will let you know once it is over.")
                .successful(true)
                .build();
    }



    @Authenticated(userTypes = { UserType.SUPER_ADMINISTRATOR })
    @GetMapping("/correct-payments")
    public ApiWorkFeedback correctPayments(){
        CompletableFuture.runAsync(()->{
            Map<Long, Institution> mapInstitutions = new HashMap<>();
            List<InstitutionReceipt> receipts = this.appRepository
                    .getInstitutionReceiptsRepository()
                    .findAll();
            receipts.parallelStream()
                    .forEach(receipt->{
                        this.appRepository.getInstitutionMemberRepository()
                                .findById(receipt.getMemberId())
                                .ifPresent(payer->{
                                    Institution institution;
                                    if(!mapInstitutions.containsKey(payer.getInstitutionId())){
                                        institution = this.appRepository.getInstitutionRepository()
                                                .findById(payer.getInstitutionId())
                                                .orElse(null);
                                        if(institution!=null) mapInstitutions.put(institution.getId(), institution);
                                    }
                                    institution = mapInstitutions.get(payer.getInstitutionId());


                                    Institution finalInstitution = institution;
                                    this.appRepository.getInstitutionDonationRepository()
                                            .findAllByReceiptNoAndInstitutionId(receipt.getReceiptNo(), finalInstitution.getId())
                                            .parallelStream()
                                            .forEach(donation->{
                                                if(finalInstitution.getInstitutionType().equals(InstitutionType.CHURCH)){
                                                    payer.addContribution(donation.getChurchContributionId());
                                                } else {
                                                    payer.addCategory(donation.getCategoryId());
                                                }

                                                payer.setInstitution(finalInstitution);
                                                PeopleSubscription subscription = PeopleSubscription.makeSubscription(payer,
                                                        Subscription.ANNUAL,
                                                        donation);
                                                this.appRepository.getPeopleSubscriptionRepository().save(subscription);
                                                this.appRepository.getInstitutionMemberRepository().save(payer);
                                            });
                                });
                    });
        });

        return ApiWorkFeedback.builder()
                .message("The process is going on. We will let you know once it is over.")
                .successful(true)
                .build();
    }


    @GetMapping("/correct-receipt-no")
    public void correctReceiptNo(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 17);
        Date startDate = calendar.getTime();
        Date monthStart = DateUtils.getFirstDateOfMonth(startDate);

        Institution institution = this.appRepository.getInstitutionRepository()
                .findById(79L).orElse(null);
        if(institution!=null) {
            Long countInBetween = this.appRepository.getInstitutionReceiptsRepository()
                    .countInBetween(monthStart, startDate, institution.getId());
            AtomicLong lastNumber = new AtomicLong(countInBetween+10000);
            List<InstitutionReceipt> receiptsFrom = this.appRepository.getInstitutionReceiptsRepository()
                    .getReceiptFromDate(startDate, institution.getId());
            receiptsFrom
                    .forEach(singleReceipt -> {
                        String receiptNo = institution.generateReceiptNo(this.appRepository, lastNumber.get());
                        singleReceipt.setReceiptNo(receiptNo);
                        this.appRepository.getInstitutionReceiptsRepository().save(singleReceipt);
                        List<InstitutionDonation> donations = this.appRepository.getInstitutionDonationRepository()
                                .findAllByReceiptNoAndInstitutionId(singleReceipt.getReceiptNo(), singleReceipt.getInstitutionId());
                        donations.parallelStream().forEach(singleDonation->{
                            singleDonation.setReceiptNo(receiptNo);
                        });
                        this.appRepository.getInstitutionDonationRepository().saveAll(donations);
                        lastNumber.set(lastNumber.get()+1);
                    });
        }
    }
}