package com.dprince.apis.events.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.configuration.files.LocalFileStorageService;
import com.dprince.configuration.files.utils.ApplicationFileType;
import com.dprince.entities.*;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.utils.AppRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data
public class Attendance implements Institutionable {
    private Long institutionId;

    @NotNull(message = "Please select the event to mark attendance for.")
    private Long eventId;

    private ChurchEvent event;
    @JsonIgnore
    public void fetchEvent(AppRepository appRepository){
        ChurchEvent event = appRepository.getChurchEventRepository()
                .findById(this.getEventId())
                .orElse(null);
        this.setEvent(event);
    }

    @NotBlank(message = "No file to upload to mark attendance")
    private String fileName;


    private void markAttendance(@NonNull List<String> memberCodes){
        List<InstitutionMember> members = appRepository.getInstitutionMemberRepository()
                .findAllByCodeIn(memberCodes);
        List<EventAttendance> attendances = new ArrayList<>();
        members.parallelStream()
                .forEach(member->{
                    // Check attended
                    boolean attended = appRepository.getEventAttendanceRepository()
                            .existsByEventIdAndMemberId(this.getEventId(), member.getId());
                    if(!attended) {
                        EventAttendance attendance = new EventAttendance();
                        attendance.setEventId(this.getEventId());
                        attendance.setMemberId(member.getId());
                        attendance.setTakenBy(this.getLoggedInUser().getId());
                        attendance.setInstitutionId(this.getInstitutionId());
                        attendances.add(attendance);
                    }
                });
        boolean marked = false;
        if(!attendances.isEmpty()){
            appRepository.getEventAttendanceRepository().saveAll(attendances);
            marked = true;
        }

        Notification ioNotification = Notification.
                createNotification(
                        this.getLoggedInUser(),
                        "Attendance Import",
                        marked ? "Imported attendance" : "Failed to mark Attendance",
                        marked ? NotificationActionType.CREATE : NotificationActionType.DELETE,
                        User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                ? null
                                : this.getInstitutionId());
        this.getAppRepository().getNotificationsRepository().save(ioNotification);
    }

    @JsonIgnore
    private User loggedInUser;
    @JsonIgnore
    private AppRepository appRepository;
    @JsonIgnore
    private LocalFileStorageService storageService;
    @JsonIgnore
    private String excelHeaders;
    @JsonIgnore
    private Reader reader;
    private static final String[] HEADERS = {"code"};
    public void doImport(@NonNull AppRepository appRepository,
                         @NonNull LocalFileStorageService storageService,
                         @NonNull User loggedInUser) {
        this.setLoggedInUser(loggedInUser);
        this.setAppRepository(appRepository);
        this.setExcelHeaders("Action Type,Title,Content,Date And Time");

        Path pathToFile = storageService.getFilesPath().resolve(this.getFileName());
        try (Reader reader = new FileReader(pathToFile.toString())) {
            this.setReader(reader);

            List<String> memberCodes = new ArrayList<>();
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .setIgnoreSurroundingSpaces(true)
                    .setIgnoreEmptyLines(true)
                    .setSkipHeaderRecord(true)
                    .setDelimiter(",")
                    .build();
            CSVParser csvParser = new CSVParser(this.getReader(), csvFormat);
            csvParser.stream().parallel()
                    .forEach(attendance -> {
                        String singleMemberCode = attendance.get("code");
                        if (!StringUtils.isEmpty(singleMemberCode)) memberCodes.add(singleMemberCode);
                    });
            if (!memberCodes.isEmpty()) this.markAttendance(memberCodes);
        } catch (IOException exception) {
            Notification ioNotification = Notification.
                    createNotification(
                            this.getLoggedInUser(),
                            "Attendance Import",
                            "Failed to process csv file. Try to upload a new one." + exception.getMessage(),
                            NotificationActionType.DELETE,
                            User.noOrganizationUsers().contains(this.getLoggedInUser().getUserType())
                                    ? null
                                    : this.getInstitutionId());
            this.getAppRepository().getNotificationsRepository().save(ioNotification);
        } finally {
            storageService.delete(this.getFileName(), ApplicationFileType.FILE);
        }
    }
}
