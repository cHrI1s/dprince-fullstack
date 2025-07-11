package com.dprince.apis.institution.models.files;

import com.dprince.apis.utils.DateUtils;
import com.dprince.configuration.files.LocalFileStorageService;
import com.dprince.configuration.files.utils.ApplicationFileType;
import com.dprince.entities.*;
import com.dprince.entities.enums.InstitutionType;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.enums.PaymentMode;
import com.dprince.entities.utils.AppRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class DonationsImporter extends ExcelImporter {
    @JsonIgnore
    private String[] organizationHeaders = {"partnerCode", "paymentMode", "category", "amount", "bankReference", "referenceNo", "accountReference", "remarks", "creditAccount", "donationDate", "receiptNo"};
    @JsonIgnore
    private String[] churchHeaders = {"memberCode", "paymentMode", "contribution", "amount", "bankReference", "referenceNo", "accountReference", "remarks", "creditAccount", "donationDate", "receiptNo"};


    private String[] getHeaders(){
        return this.getInstitution()
                .getInstitutionType()
                .equals(InstitutionType.CHURCH)
                ? this.getChurchHeaders()
                : this.getOrganizationHeaders();
    }

    public void populateWithCommon(final CSVRecord csvRecord,
                                   List<Notification> notifications,
                                   int position){
        String title = "Donation Import - Row : "+(position+1);
        try {
            String institutionName = this.getInstitution().getInstitutionTypeName(),
                    memberTypeName = this.getInstitution().getInstitutionType().equals(InstitutionType.CHURCH)
                            ? "Member"
                            : "Partner";

            InstitutionDonation institutionDonation = new InstitutionDonation();

            String memberCode = csvRecord.get(this.getInstitution()
                    .getInstitutionType()
                    .equals(InstitutionType.CHURCH) ? "memberCode" : "partnerCode").trim();
            InstitutionMember member = this.getAppRepository()
                    .getInstitutionMemberRepository()
                    .findByCodeAndInstitutionId(memberCode, this.getInstitution().getId()).orElse(null);
            if (member == null) {
                notifications.add(this.generateNotification(position,
                        memberTypeName + " \"" + memberCode + " does not exist in " + institutionName + "."));
                return;
            }
            institutionDonation.setMemberId(member.getId());

            String paymentModeString = csvRecord.get("paymentMode").trim();
            if (!StringUtils.isEmpty(paymentModeString)) {
                try {
                    PaymentMode paymentMode = PaymentMode.valueOf(paymentModeString);
                    institutionDonation.setPaymentMode(paymentMode);
                } catch(Exception e){
                    notifications.add(this.generateNotification(position,
                            "Payment mode \""+paymentModeString+"\" not recognized."));
                    return;
                }
            } else {
                notifications.add(this.generateNotification(position,
                        "Payment mode not specified."));
                return;
            }

            if (this.getInstitution().getInstitutionType().equals(InstitutionType.CHURCH)) {
                String churchContributionString = csvRecord.get("contribution").trim();
                if (!StringUtils.isEmpty(churchContributionString)) {
                    ChurchContribution churchContribution = this.getAppRepository()
                            .getChurchContributionRepository()
                            .findByNameAndInstitutionId(churchContributionString, this.getInstitution().getId());
                    if (churchContribution == null) {
                        notifications.add(this.generateNotification(position,
                                "Contribution \"" + churchContributionString + "\" was not recognized."));
                        return;
                    }
                    institutionDonation.setChurchContributionId(churchContribution.getId());
                } else {
                    notifications.add(this.generateNotification(position,
                            "Contribution is not specified"));
                    return;
                }
            } else {
                String categoryString = csvRecord.get("category").trim();
                if (!StringUtils.isEmpty(categoryString)) {
                    InstitutionCategory category = this.getAppRepository().getCategoryRepository()
                            .findByNameAndInstitutionId(categoryString, this.getInstitution().getId());
                    if (category == null) {
                        notifications.add(this.generateNotification(position,
                                "Category \"" + categoryString + "\" was not recognized."));
                        return;
                    }
                    institutionDonation.setCategoryId(category.getId());
                } else {
                    notifications.add(this.generateNotification(position,
                            "Category is not specified."));
                    return;
                }
            }

            String amountString = csvRecord.get("amount").trim();
            if (!StringUtils.isEmpty(amountString)) {
                long amount = Long.parseLong(amountString);
                if(amount>=5) {
                    institutionDonation.setAmount(amount);
                } else {
                    notifications.add(this.generateNotification(position,
                            "The amount should not be lesser than Rs. 5"));
                    return;
                }
            } else {
                notifications.add(this.generateNotification(position,
                        "Receipt should have amount specified."));
                return;
            }

            /*
            String bankReference = csvRecord.get("bankReference").trim();
            if (!StringUtils.isEmpty(bankReference)) institutionDonation.setBankReference(bankReference);

            String referenceNo = csvRecord.get("referenceNo").trim();
            if (!StringUtils.isEmpty(referenceNo)) institutionDonation.setReferenceNo(referenceNo);

            String accountReference = csvRecord.get("accountReference").trim();
            if (!StringUtils.isEmpty(accountReference)) institutionDonation.setReferenceAccount(accountReference);

            String remarks = csvRecord.get("remarks").trim();
            if (!StringUtils.isEmpty(remarks)) institutionDonation.setRemarks(remarks);
             */

            String creditAccountString = csvRecord.get("creditAccount").trim();
            if (!StringUtils.isEmpty(creditAccountString)) {
                InstitutionCreditAccount creditAccount = this.getAppRepository().getCreditAccountsRepository()
                        .findByNameAndInstitutionId(creditAccountString, this.getInstitutionId());
                if (creditAccount != null) institutionDonation.setCreditAccount(creditAccount.getId());
            }

            String donationDateString = csvRecord.get("donationDate").trim();
            Date donationDate = DateUtils.getNowDateTime();
            if (!StringUtils.isEmpty(donationDateString)) {
                try {
                    donationDate = this.getDateFromString(donationDateString);
                } catch (Exception e){
                    notifications.add(this.generateNotification(position,
                            "The donation date is invalid."));
                    return;
                }
            }
            institutionDonation.setEntryDate(donationDate);

            String receiptNo = csvRecord.get("receiptNo").trim();
            InstitutionReceipt institutionReceipt = this.getAppRepository().getInstitutionReceiptsRepository()
                    .findByReceiptNoAndInstitutionId(receiptNo, this.getInstitutionId())
                    .orElse(null);
            if (institutionReceipt != null) {
                institutionDonation.setReceiptNo(institutionReceipt.getReceiptNo());
                institutionDonation.setReceipt(institutionReceipt);
            } else {
                institutionDonation.setReceiptNo(receiptNo);
            }

            institutionDonation.setInstitution(this.getInstitution());
            institutionDonation.setInstitutionId(this.getInstitution().getId());
            institutionDonation.setMember(member);
            boolean isAlreadySaved = institutionDonation.isAlreadyPresent(this.getAppRepository());

            String message = "Failed to Import Donation";
            NotificationActionType notificationActionType = NotificationActionType.DELETE;
            if(!isAlreadySaved){
                institutionDonation.save(this.getAppRepository());
                notificationActionType = NotificationActionType.CREATE;
                message = "Successfully imported donation.";
            }

            Notification successNotification = Notification.createNotification(this.getLoggedInUser(),
                    this.getDefaultHeader()+" : "+position,
                    message,
                    notificationActionType,
                    this.getInstitution().getId());
            notifications.add(successNotification);
        } catch (Exception e){
            notifications.add(this.generateNotification(position,
                    "Failed to save the donation."));
        }
    }


    public void doImport(@NonNull AppRepository appRepository,
                         @NonNull LocalFileStorageService storageService,
                         @NonNull User loggedInUser){
        this.setLoggedInUser(loggedInUser);
        this.setAppRepository(appRepository);
        this.setStorageService(storageService);
        this.setExcelHeaders("Action Type,Title,Content,Date And Time");

        List<Notification> notifications = new ArrayList<>();
        Path pathToFile = this.getStorageService().getFilesPath().resolve(this.getName());

        try (Reader reader = new FileReader(pathToFile.toString())) {
            this.setReader(reader);
            this.setDefaultHeader("Donation at row ");

            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(this.getHeaders())
                    .setIgnoreSurroundingSpaces(true)
                    .setIgnoreEmptyLines(true)
                    .setSkipHeaderRecord(true)
                    .setDelimiter(",")
                    .build();
            CSVParser csvParser = new CSVParser(this.getReader(), csvFormat);
            int position = 0;
            for (CSVRecord csvRecord : csvParser) {
                position++;
                this.populateWithCommon(csvRecord, notifications, position);
            }
            Notification ioNotification = Notification.
                    createNotification(
                            this.getLoggedInUser(),
                            "Donations Import",
                            "Finished to import donations.",
                            NotificationActionType.READ,
                            User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                    ? null
                                    : this.getInstitutionId());
            notifications.add(ioNotification);
        } catch (IOException exception){
            Notification ioNotification = Notification.
                    createNotification(
                            this.getLoggedInUser(),
                            this.getDefaultHeader(),
                            "Failed to process csv file. Try to upload a new one.",
                            NotificationActionType.DELETE,
                            User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                    ? null
                                    : this.getInstitutionId());
            notifications.add(ioNotification);
        } finally {
            if(!notifications.isEmpty()) this.getAppRepository().getNotificationsRepository().saveAll(notifications);
            if(this.getStorageService()!=null) {
                this.getStorageService()
                        .delete(this.getName(), ApplicationFileType.FILE);
            }
            this.saveIntoFile(notifications);
        }
    }
}
