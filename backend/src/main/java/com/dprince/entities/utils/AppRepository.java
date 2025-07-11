package com.dprince.entities.utils;

import com.dprince.entities.repositories.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Data
@Service
@RequiredArgsConstructor
public class AppRepository {
    private final UsersRepository usersRepository;
    private final LoginDetailsRepository loginDetailsRepository;
    private final LoginTokensRepository loginTokensRepository;
    private final PersonTitlesRepository personTitlesRepository;
    private final InstitutionRepository institutionRepository;
    private final InstitutionMemberRepository institutionMemberRepository;
    private final InstitutionFamilyRepository institutionFamilyRepository;
    private final InstitutionFamilyMemberRepository institutionFamilyMemberRepository;
    private final CategoryRepository categoryRepository;
    private final PriestSignatureRepository priestSignatureRepository;
    private final TemplateRepository templateRepository;
    private final CertificatesBackgroundsRepository certificatesBackgroundsRepository;
    private final CertificatesRepository certificatesRepository;

    private final ChurchEventRepository churchEventRepository;
    private final InstitutionReceiptsRepository institutionReceiptsRepository;
    private final InstitutionDonationRepository institutionDonationRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final GroupRepository groupRepository;
    private final ChurchContributionRepository churchContributionRepository;
    private final InstitutionCommunicationRepository communicationRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final NotificationsRepository notificationsRepository;
    private final MemberActivityRepository memberActivityRepository;
    private final OtpRepository otpRepository;
    private final GuestPriestsRepository guestPriestsRepository;
    private final InstitutionCreditAccountsRepository creditAccountsRepository;
    private final RenewalsRepository renewalsRepository;
    private final PincodeRepository pincodeRepository;
    private final ReceiptAcknowledgementRepository receiptAcknowledgementRepository;
    private final TopUpRepository topUpRepository;
    private final EventAttendanceRepository eventAttendanceRepository;
    private final PeopleSubscriptionRepository peopleSubscriptionRepository;


    private final FileTokensRepository fileTokensRepository;
    private final CronJobRepository cronJobRepository;
    private final MailSenderRepository mailSenderRepository;
}
