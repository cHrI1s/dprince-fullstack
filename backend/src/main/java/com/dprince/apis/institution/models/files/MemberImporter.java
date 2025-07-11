package com.dprince.apis.institution.models.files;

import com.dprince.configuration.files.LocalFileStorageService;
import com.dprince.configuration.files.utils.ApplicationFileType;
import com.dprince.entities.*;
import com.dprince.entities.enums.*;
import com.dprince.entities.utils.AppRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public class MemberImporter extends ExcelImporter {


    @JsonIgnore
    private String[] organizationHeaders = {"title", "code", "firstName", "lastName", "gender", "dob", "dom", "email", "subscription", "categories", "status", "language", "creationDate", "addressLine1", "addressLine2", "addressLine3", "pincode", "district", "state", "country", "phoneCode", "phone", "alternatePhoneCode", "alternatePhone", "whatsappNumberCode", "whatsappNumber", "landlineNumberCode", "landlineNumber"};
    @JsonIgnore
    private String[] churchHeaders = {"title", "code", "firstName", "lastName", "gender", "dob", "dom", "baptized", "dateBaptism", "communion", "dateCommunion", "email", "status", "language", "creationDate", "addressLine1", "addressLine2", "addressLine3", "pincode", "district", "state", "country", "phoneCode", "phone", "alternatePhoneCode", "alternatePhone", "whatsappNumberCode", "whatsappNumber", "landlineNumberCode", "landlineNumber"};


    private void populateWithCommonData(InstitutionMember member,
                                        CSVRecord csvRecord,
                                        List<Notification> notifications,
                                        int position){
        String personType = this.getInstitution().getInstitutionType().equals(InstitutionType.CHURCH)
                ? "Member"
                : "Partner";
        Notification notification;
        InstitutionMember savedMember;
        PersonTitle personTitle = this.getAppRepository().getPersonTitlesRepository()
                .findByShortName(csvRecord.get("title"))
                .orElse(null);
        if (personTitle == null) {
            notifications.add(this.generateNotification(position,
                    "Title: '" + csvRecord.get("title").trim() + "' is not recognized."));
            return;
        }

        member.setInstitutionId(this.getInstitutionId());
        member.setTitleId(personTitle.getId());


        String memberCode = csvRecord.get("code").trim();
        if (!StringUtils.isEmpty(memberCode)) {
            member.setCode(memberCode);
            savedMember = this.getAppRepository().getInstitutionMemberRepository()
                    .findByCodeAndInstitutionId(memberCode, this.getInstitutionId())
                    .orElse(null);
            if (savedMember != null) {
                Notification notificationForMember = new Notification();
                notificationForMember.setTitle(this.getDefaultHeader()+" : "+position);
                notificationForMember.setContent(personType+" code '" + memberCode + "' is already used by someone else.");
                notificationForMember.setInstitutionId(this.getInstitutionId());
                if(this.getLoggedInUser()!=null) {
                    notificationForMember.setExecutor(this.getLoggedInUser().getId());
                    notificationForMember.setExecutorType(this.getLoggedInUser().getUserType());
                }
                notificationForMember.setActionType(NotificationActionType.READ);
                notifications.add(notificationForMember);
                return;
            }
        }
        member.setFirstName(csvRecord.get("firstName").trim());
        String lastName = csvRecord.get("lastName").trim();
        if(StringUtils.isEmpty(lastName)) lastName = ".";
        member.setLastName(lastName);

        String genderString = csvRecord.get("gender").trim();
        try {
            Gender gender = StringUtils.isEmpty(genderString)
                    ? Gender.NOT_DEFINED
                    : Gender.valueOf(genderString);
            member.setGender(gender);
        } catch (Exception ignored){
            notifications.add(this.generateNotification(position,
                    "Gender "+genderString+"\" is not valid."));
            return;
        }

        String dobString = csvRecord.get("dob").trim();
        if (!StringUtils.isEmpty(dobString)) {
            try {
                Date dobDate = this.getDateFromString(dobString);
                member.setDob(dobDate);
            } catch(Exception ignored){
                notifications.add(this.generateNotification(position,
                        "Date of birth format is invalid."));
                return;
            }
        }

        String domString = csvRecord.get("dom").trim();
        if (!StringUtils.isEmpty(domString)) {
            try {
                Date domDate = this.getDateFromString(domString);
                member.setDom(domDate);
            } catch(Exception ignored){
                notifications.add(this.generateNotification(position,
                        "Date of Marriage format is invalid."));
                return;
            }
        }

        String emailString = csvRecord.get("email").trim();
        if (!StringUtils.isEmpty(emailString)) {
            member.setEmail(emailString);
            /*
            savedMember = this.getAppRepository().getInstitutionMemberRepository()
                    .findByEmailAndInstitutionId(emailString, this.getInstitutionId())
                    .orElse(null);
            if (savedMember != null) {
                notifications.add(this.generateNotification(position,
                        "There is already someone using that email."));
                return;
            }
             */
        }

        String statusString = csvRecord.get("status").trim();
        try {
            MemberStatus status = MemberStatus.valueOf(statusString);
            member.setStatus(statusString.equals("INACTIVE") ? MemberStatus.INACTIVE : status);
        } catch (Exception ignored){
            notifications.add(this.generateNotification(position,
                    "Member status \""+statusString+"\" is not valid."));
            return;
        }

        try {
            AppLanguage language = AppLanguage.valueOf(csvRecord.get("language").trim());
            member.setLanguage(language);
        } catch (Exception e){
            notifications.add(this.generateNotification(position,
                    "The language is not valid."));
            return;
        }

        String creationDateString = csvRecord.get("creationDate").trim();
        if(!StringUtils.isEmpty(creationDateString)){
            try {
                Date creationDate = this.getDateFromString(creationDateString);
                member.setCreationDate(creationDate);
            } catch(Exception ignored){
                notifications.add(this.generateNotification(position,
                        "Creation Date format is invalid."));
                return;
            }
        }

        member.setAddressLine1(csvRecord.get("addressLine1").trim());
        member.setAddressLine2(csvRecord.get("addressLine2").trim());
        member.setAddressLine3(csvRecord.get("addressLine3").trim());

        String pincodeString = csvRecord.get("pincode").trim();
        if(!StringUtils.isEmpty(pincodeString)) {
            try {
                member.setPincode(Integer.parseInt(pincodeString));
            } catch (Exception ignored) {
                member.setPincode(null);
            }
        } else {
            member.setPincode(null);
        }
        member.setDistrict(csvRecord.get("district").trim());
        member.setState(csvRecord.get("state").trim());
        member.setCountry(csvRecord.get("country").trim());

        String phoneCodeString = csvRecord.get("phoneCode").trim();
        long phoneCode = 91L;
        if(!StringUtils.isEmpty(phoneCodeString)) {
            try {
                phoneCode = Long.parseLong(phoneCodeString);
            } catch (Exception ignored){
                notifications.add(this.generateNotification(position,
                        "Phone code is not a valid."));
                return;
            }
        }
        String phoneString = csvRecord.get("phone").trim();
        if (!StringUtils.isEmpty(phoneString)) {
            try {
                Long phone = Long.parseLong(phoneString);
                member.setPhone(phone);
                member.setPhoneCode(phoneCode);
                /*
                savedMember = this.getAppRepository().getInstitutionMemberRepository()
                        .findByPhoneCodeAndPhoneAndInstitutionId(phoneCode, phone, this.getInstitutionId());
                if (savedMember != null) {
                    notification = Notification.
                            createNotification(
                                    this.getLoggedInUser(),
                                    personType+" Import",
                                    "There is already someone by using that phone number.",
                                    NotificationActionType.DELETE,
                                    User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                            ? null
                                            : this.getInstitutionId());
                    notifications.add(notification);
                    return;
                }*/
            } catch (Exception ignored){
                notifications.add(this.generateNotification(position,
                        "Phone number is not a valid."));
                return;
            }
        }

        String alternatePhoneCode = csvRecord.get("alternatePhoneCode").trim();
        if (!StringUtils.isEmpty(alternatePhoneCode)){
            try {
                member.setAlternatePhoneCode(Long.parseLong(alternatePhoneCode));
            } catch (Exception ignored){
                notifications.add(this.generateNotification(position,
                        "Alternate Phone code is not a valid."));
                return;
            }
        }

        String alternatePhone = csvRecord.get("alternatePhone").trim();
        if (!StringUtils.isEmpty(alternatePhone)){
            try {
                member.setAlternatePhone(Long.parseLong(alternatePhone));
            } catch (Exception ignored){
                notifications.add(this.generateNotification(position,
                        "Alternate phone number is not a valid."));
                return;
            }
        }

        String whatsappNumberCode = csvRecord.get("whatsappNumberCode").trim();
        if (!StringUtils.isEmpty(whatsappNumberCode)){
            try {
                member.setAlternatePhoneCode(Long.parseLong(csvRecord.get("whatsappNumberCode")));
            } catch (Exception ignored){
                notifications.add(this.generateNotification(position,
                        "Whatsapp Number Code is not a valid."));
                return;
            }
        }

        String whatsappNumber = csvRecord.get("whatsappNumber").trim();
        if (!StringUtils.isEmpty(whatsappNumber)){
            try {
                member.setAlternatePhone(Long.parseLong(csvRecord.get("whatsappNumber")));
            } catch (Exception ignored){
                notifications.add(this.generateNotification(position,
                        "Whatsapp Number is not a valid."));
                return;
            }
        }

        String landlineNumberCode = csvRecord.get("landlineNumberCode").trim();
        if (!StringUtils.isEmpty(landlineNumberCode)){
            try {
                member.setLandlinePhoneCode(Long.parseLong(csvRecord.get("landlineNumberCode")));
            } catch (Exception ignored){
                notifications.add(this.generateNotification(position,
                        "Landline Number Code is not a valid."));
                return;
            }
        }

        String landlineNumber = csvRecord.get("landlineNumber").trim();
        if (!StringUtils.isEmpty(landlineNumber)){
            try {
                member.setLandlinePhone(Long.parseLong(csvRecord.get("landlineNumber")));
            } catch (Exception ignored){
                notifications.add(this.generateNotification(position,
                        "Landline Number is not a valid."));
                return;
            }
        }


        if(this.getInstitution().getInstitutionType().equals(InstitutionType.GENERAL)) {
            member.setImported(true);
            member.adjustDeadline(false);
        }
        // Save the member
        member = this.getAppRepository().getInstitutionMemberRepository().save(member);

        if(this.getInstitution().getInstitutionType().equals(InstitutionType.GENERAL)){
            if(member.getCategoriesIds()!=null && !member.getCategoriesIds().isEmpty()){
                final InstitutionMember theMember = member;
                member.getCategoriesIds().parallelStream()
                        .forEach(singleCategoryId->{
                            InstitutionDonation donation = new InstitutionDonation();
                            donation.setEntryDate(theMember.getCreationDate());
                            donation.setCategoryId(singleCategoryId);
                            PeopleSubscription.makeSubscription(theMember,
                                    theMember.getSubscription(),
                                    donation);
                        });
            }
        }
        notification = Notification.
                createNotification(
                        this.getLoggedInUser(),
                        personType+" Import",
                        "Successfully Imported "+personType+" at position " + (position + 1) + ".",
                        NotificationActionType.CREATE,
                        User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                ? null
                                : this.getInstitutionId());
        this.getInstitution().incrementMembersCount();
        // Mae sure the institution members count gets increased.
        this.getAppRepository().getInstitutionRepository().save(this.getInstitution());
        this.getAppRepository().getNotificationsRepository().save(notification);
    }


    private void importOrganizationMembers(SubscriptionPlan subscriptionPlan){
        int position = 0;
        List<Notification> notifications = new ArrayList<>();
        try {
            this.setDefaultHeader("Import of members - Row");
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(this.getOrganizationHeaders())
                    .setIgnoreSurroundingSpaces(true)
                    .setIgnoreEmptyLines(true)
                    .setSkipHeaderRecord(true)
                    .setDelimiter(",")
                    .build();
            CSVParser csvParser = new CSVParser(this.getReader(), csvFormat);
            if (csvParser.getRecordNumber() >= subscriptionPlan.getMembers()) {
                Notification noQuotaNotification = Notification.
                        createNotification(
                                this.getLoggedInUser(),
                                "Partners Import",
                                "Please upgrade your plan to import these Partners.",
                                NotificationActionType.DELETE,
                                User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                        ? null
                                        : this.getInstitutionId());
                notifications.add(noQuotaNotification);
            } else {
                // Loop through the rows
                for (CSVRecord csvRecord : csvParser) {
                    position++;
                    Notification notification = null;
                    try {
                        InstitutionMember member = new InstitutionMember();
                        String subscriptionString = csvRecord.get("subscription").trim();
                        if(StringUtils.isEmpty(subscriptionString)){
                            notifications.add(this.generateNotification(position,
                                    "Subscription is not known at "+position));
                            continue;
                        }
                        try {
                            Subscription subscription = Subscription.valueOf(subscriptionString);
                            member.setSubscription(subscription);
                        } catch (Exception e){
                            notifications.add(this.generateNotification(position,
                                    "Subscription is not valid at "+position));
                            continue;
                        }

                        String categoriesString = csvRecord.get("categories").trim();
                        List<String> categoriesListString = Arrays.asList(categoriesString.split(";"));
                        List<Long> categoryList = this.getAppRepository().getCategoryRepository()
                                .findAllByNameInAndInstitutionId(categoriesListString, this.getInstitutionId())
                                .stream()
                                .map(InstitutionCategory::getId).collect(Collectors.toList());
                        if(categoryList.isEmpty()){
                            notifications.add(this.generateNotification(position,
                                    "Categories is/are not known at "+position));
                            continue;
                        }
                        member.setCategoriesIds(new HashSet<>(categoryList));


                        this.populateWithCommonData(member, csvRecord, notifications, position);
                    } catch (Exception exception) {
                        // This may be a database error;
                        notification = Notification.
                                createNotification(
                                        this.getLoggedInUser(),
                                        "Partners Import",
                                        "Failed to save partner at position " + (position + 1) + ".",
                                        NotificationActionType.DELETE,
                                        User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                                ? null
                                                : this.getInstitutionId());
                    } finally {
                        if (notification != null) notifications.add(notification);
                    }
                }

                if (position == 0) {
                    Notification emptyNotification = Notification.
                            createNotification(
                                    this.getLoggedInUser(),
                                    "Partners Import",
                                    "Provided CSV File was empty.",
                                    NotificationActionType.READ,
                                    User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                            ? null
                                            : this.getInstitutionId());
                    notifications.add(emptyNotification);
                }
            }
        } catch(Exception exception){
            Notification ioNotification = Notification.
                    createNotification(
                            this.getLoggedInUser(),
                            "Partners Import",
                            "Failed to process csv file. Try to upload a new one." + exception.getMessage(),
                            NotificationActionType.DELETE,
                            User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                    ? null
                                    : this.getInstitutionId());
            notifications.add(ioNotification);
        } finally {
            Notification finalNotification = Notification.
                    createNotification(
                            this.getLoggedInUser(),
                            "Partners Import",
                            "Import processing of File '"+this.getName()+"' is done.",
                            NotificationActionType.READ,
                            User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                    ? null
                                    : this.getInstitutionId());
            notifications.add(finalNotification);

            // Delete the file
            this.getStorageService().delete(this.getName(), ApplicationFileType.FILE);
            this.getAppRepository().getNotificationsRepository().saveAll(notifications);

            this.saveIntoFile(notifications);
        }
    }


    private void importChurchMembers(SubscriptionPlan subscriptionPlan){
        int position = 0;
        List<Notification> notifications = new ArrayList<>();
        try {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(this.getChurchHeaders())
                    .setIgnoreSurroundingSpaces(true)
                    .setIgnoreEmptyLines(true)
                    .setSkipHeaderRecord(true)
                    .setDelimiter(",")
                    .build();
            CSVParser csvParser = new CSVParser(this.getReader(), csvFormat);

            if (csvParser.getRecordNumber() >= subscriptionPlan.getMembers()) {
                Notification noQuotaNotification = Notification.
                        createNotification(
                                this.getLoggedInUser(),
                                "Church Member Import",
                                "Please upgrade your plan to import these members.",
                                NotificationActionType.DELETE,
                                User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                        ? null
                                        : this.getInstitutionId());
                notifications.add(noQuotaNotification);
            } else {
                // Loop through the rows
                for (CSVRecord csvRecord : csvParser) {
                    Notification notification = null;
                    try {
                        InstitutionMember member = new InstitutionMember();
                        String communionString = csvRecord.get("communion").trim();
                        if(!StringUtils.isEmpty(communionString)) {
                            Boolean communion = Integer.parseInt(communionString)==1;
                            member.setCommunion(communion);
                        }

                        String communionDateString = csvRecord.get("dateCommunion").trim();
                        if(!StringUtils.isEmpty(communionDateString)) {
                            try {
                                Date communionDate = this.getDateFromString(communionDateString);
                                member.setCommunionDate(communionDate);
                            } catch (Exception ignored){
                                notifications.add(this.generateNotification(position,
                                        "Date of communion is not valid."));
                            }
                        }

                        String baptizedString = csvRecord.get("baptized").trim();
                        if(!StringUtils.isEmpty(baptizedString)) {
                            try {
                                BaptismStatus baptismStatus = BaptismStatus.valueOf(baptizedString);
                                member.setBaptized(baptismStatus);
                            } catch (Exception e){
                                notifications.add(this.generateNotification(position,
                                        "Date of baptism is not valid."));
                            }
                        }

                        this.populateWithCommonData(member, csvRecord, notifications, position);
                    } catch (Exception exception) {
                        // This may be a database error;
                        notification = Notification.
                                createNotification(
                                        this.getLoggedInUser(),
                                        "Member Import",
                                        "Failed to save member at position " + (position + 1) + ".",
                                        NotificationActionType.DELETE,
                                        User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                                ? null
                                                : this.getInstitutionId());
                    } finally {
                        if (notification != null) notifications.add(notification);
                        position++;
                    }
                }

                if (position == 0) {
                    Notification emptyNotification = Notification.
                            createNotification(
                                    this.getLoggedInUser(),
                                    "Partners Import",
                                    "Provided CSV File was empty.",
                                    NotificationActionType.READ,
                                    User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                            ? null
                                            : this.getInstitutionId());
                    notifications.add(emptyNotification);
                }
            }
        } catch(Exception exception){
            Notification ioNotification = Notification.
                    createNotification(
                            this.getLoggedInUser(),
                            "Partners Import",
                            "Failed to process csv file. Try to upload a new one." + exception.getMessage(),
                            NotificationActionType.DELETE,
                            User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                    ? null
                                    : this.getInstitutionId());
            notifications.add(ioNotification);
        } finally {
            Notification finalNotification = Notification.
                    createNotification(
                            this.getLoggedInUser(),
                            "Partners Import",
                            "Import processing of File '"+this.getName()+"' is done.",
                            NotificationActionType.READ,
                            User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                    ? null
                                    : this.getInstitutionId());
            notifications.add(finalNotification);

            // Delete the file
            this.getStorageService().delete(this.getName(), ApplicationFileType.FILE);

            this.saveIntoFile(notifications);
        }
    }



    public void doImport(@NonNull AppRepository appRepository,
                         @NonNull LocalFileStorageService storageService,
                         @NonNull User loggedInUser){
        this.setLoggedInUser(loggedInUser);
        this.setAppRepository(appRepository);
        this.setStorageService(storageService);
        this.setExcelHeaders("Action Type,Title,Content,Date And Time");

        Path pathToFile = this.getStorageService().getFilesPath().resolve(this.getName());
        try (Reader reader = new FileReader(pathToFile.toString())) {
            this.setReader(reader);
            SubscriptionPlan subscriptionPlan = this.getAppRepository().getSubscriptionPlanRepository()
                    .findById(this.getInstitution().getSubscriptionPlan())
                    .orElseThrow(()->{
                        String message = "Subscription plan not recognized.";
                        return new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                                message);
                    });

            if(this.getInstitution().getInstitutionType().equals(InstitutionType.GENERAL)) this.importOrganizationMembers(subscriptionPlan);
            else this.importChurchMembers(subscriptionPlan);
        } catch (IOException exception){
            Notification ioNotification = Notification.
                    createNotification(
                            this.getLoggedInUser(),
                            "Partners Import",
                            "Failed to process csv file. Try to upload a new one." + exception.getMessage(),
                            NotificationActionType.DELETE,
                            User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                    ? null
                                    : this.getInstitutionId());
            this.getAppRepository().getNotificationsRepository().save(ioNotification);
        }
    }
}
