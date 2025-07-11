package com.dprince.apis.institution.models.files;

import com.dprince.apis.utils.DateUtils;
import com.dprince.configuration.files.LocalFileStorageService;
import com.dprince.configuration.files.utils.ApplicationFileType;
import com.dprince.entities.InstitutionFamily;
import com.dprince.entities.InstitutionMember;
import com.dprince.entities.Notification;
import com.dprince.entities.User;
import com.dprince.entities.enums.InstitutionType;
import com.dprince.entities.enums.MemberOfFamilyTitle;
import com.dprince.entities.enums.NotificationActionType;
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
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FamiliesImporter extends ExcelImporter {
    @JsonIgnore
    private String[] headers = {"hofCode", "hofRole","hofMemberSince",
            "familyMemberCode",
            "familyRole", "memberSince"};


    public void doImport(@NonNull AppRepository appRepository,
                         @NonNull LocalFileStorageService storageService,
                         @NonNull User loggedInUser){
        this.setLoggedInUser(loggedInUser);
        this.setAppRepository(appRepository);
        this.setStorageService(storageService);
        this.setExcelHeaders("Action Type,Title,Content,Date And Time");
        this.setDefaultHeader("Families Import - Row");

        List<Notification> notifications = new ArrayList<>();
        Path pathToFile = this.getStorageService().getFilesPath().resolve(this.getName());

        try (Reader reader = new FileReader(pathToFile.toString())) {
            this.setReader(reader);

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
                InstitutionMember familyMember = new InstitutionMember();

                String mainPartnerCode = csvRecord.get("hofCode").trim();
                if(StringUtils.isEmpty(mainPartnerCode)){
                    notifications.add(this.generateNotification(position, "Partner code should not be null."));
                    continue;
                }
                InstitutionMember mainMember = this.getAppRepository()
                        .getInstitutionMemberRepository()
                        .findByCodeAndInstitutionId(mainPartnerCode,
                                this.getInstitution().getId())
                        .orElse(null);
                if(mainMember==null){
                    notifications.add(this.generateNotification(position,
                            (this.getInstitution().getInstitutionType()
                                    .equals(InstitutionType.GENERAL)
                                    ? "Partner" : "Member") + " does not exist."));
                    continue;
                }

                String addressLine1 = StringUtils.isEmpty(mainMember.getAddressLine1()) ? "." : mainMember.getAddressLine1();
                String addressLine2 = StringUtils.isEmpty(mainMember.getAddressLine2()) ? "." : mainMember.getAddressLine2();
                String addressLine3 = StringUtils.isEmpty(mainMember.getAddressLine3()) ? "." : mainMember.getAddressLine3();
                String district = StringUtils.isEmpty(mainMember.getDistrict()) ? "Undefined" : mainMember.getDistrict();
                InstitutionFamily institutionFamily = mainMember.getFamilyWhereHead(this.getAppRepository());
                if(institutionFamily==null){
                    // Create a new family
                    InstitutionFamily newFamily = new InstitutionFamily();
                    newFamily.setFamilyHead(mainMember.getId());
                    newFamily.setName(mainMember.getFullName());
                    newFamily.setInstitutionId(mainMember.getInstitutionId());
                    newFamily.setDob(mainMember.getDom());
                    newFamily.setAddressLine1(addressLine1);
                    newFamily.setAddressLine2(addressLine2);
                    newFamily.setAddressLine3(addressLine3);
                    newFamily.setState(mainMember.getState());
                    newFamily.setPincode(mainMember.getPincode()==null ? 0 : mainMember.getPincode());
                    newFamily.setDistrict(district);
                    newFamily.setPhone(mainMember.getPhone());
                    newFamily.setPhoneCode(mainMember.getPhoneCode());
                    institutionFamily = this.getAppRepository().getInstitutionFamilyRepository()
                            .save(newFamily);
                }

                String hofRoleString = csvRecord.get("hofRole").trim();
                if(!StringUtils.isEmpty(hofRoleString)){
                    try {
                        MemberOfFamilyTitle role = MemberOfFamilyTitle.valueOf(hofRoleString);
                        mainMember.setFamilyRole(role);
                    } catch (Exception ignored) {
                        notifications.add(this.generateNotification(position,
                                "Family Role unknown!"));
                        continue;
                    }
                } else {
                    notifications.add(this.generateNotification(position,
                            "HOF role is not specified!"));
                    continue;
                }

                String hofMemberSince = csvRecord.get("hofMemberSince").trim();
                if(!StringUtils.isEmpty(hofMemberSince)){
                    try {
                        Date hofMemberSinceDate = this.getDateFromString(hofMemberSince);
                        mainMember.setMemberSince(hofMemberSinceDate);
                    } catch (Exception ignored) {
                        notifications.add(this.generateNotification(position,
                                "Family Role unknown!"));
                        continue;
                    }
                } else {
                    notifications.add(this.generateNotification(position,
                            "HOF role is not specified!"));
                    continue;
                }
                institutionFamily.addMember(mainMember);

                String familyMemberCode = csvRecord.get("familyMemberCode").trim();
                if(StringUtils.isEmpty(familyMemberCode)){
                    notifications.add(this.generateNotification(position,
                            "Family Member code should not be empty."));
                    continue;
                }
                InstitutionMember savedFamilyMember = this.getAppRepository()
                        .getInstitutionMemberRepository()
                        .findByCodeAndInstitutionId(familyMemberCode, this.getInstitutionId())
                        .orElse(null);
                if(savedFamilyMember!=null){
                    familyMember = savedFamilyMember;
                } else {
                    notifications.add(this.generateNotification(position, "No Such Person found!"));
                    continue;
                }

                String familyRoleString = csvRecord.get("familyRole").trim();
                if(StringUtils.isEmpty(familyRoleString)){
                    notifications.add(this.generateNotification(position,
                            "Please specify the title in the family"));
                    continue;
                }
                try {
                    MemberOfFamilyTitle familyTitleEnum = MemberOfFamilyTitle.valueOf(familyRoleString);
                    familyMember.setFamilyRole(familyTitleEnum);
                } catch (Exception e){
                    notifications.add(this.generateNotification(position,
                            "Family Role \""+familyRoleString+"\" is not known."));
                    continue;
                }

                String memberSinceString = csvRecord.get("memberSince").trim();
                if(!StringUtils.isEmpty(memberSinceString)){
                    Date memberSince = this.getDateFromString(memberSinceString);
                    familyMember.setMemberSince(memberSince);
                } else {
                    familyMember.setMemberSince(DateUtils.getNowDateTime());
                }

                boolean saved = familyMember.saveAsFamilyMemberOf(this.getAppRepository(), institutionFamily);
                if(!saved) {
                    notifications.add(
                            Notification.createNotification(
                                    loggedInUser,
                                    this.getDefaultHeader()+" : "+position,
                                    "Something went wrong. Failed to save family.",
                                    NotificationActionType.UPDATE,
                                    institutionFamily.getInstitutionId()
                            )
                    );
                } else {
                    notifications.add(
                            Notification.createNotification(
                                    loggedInUser,
                                    this.getDefaultHeader()+" : "+position,
                                    "Member \""+familyMember.getFullName()+"\"successfully added to the family.",
                                    NotificationActionType.CREATE,
                                    institutionFamily.getInstitutionId()
                            )
                    );
                }
            }
        } catch (Exception exception){
            exception.printStackTrace();
            notifications.add(this.generateNotification(null,
                    "Failed to process csv file. Try to upload a new one."));
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
