package com.dprince.scheduler.task;

import com.dprince.apis.institution.models.AddressRequestModel;
import com.dprince.apis.institution.models.enums.DurationGap;
import com.dprince.apis.institution.vos.Receiptor;
import com.dprince.apis.utils.DateUtils;
import com.dprince.configuration.email.EmailService;
import com.dprince.configuration.email.util.EmailToSend;
import com.dprince.entities.Institution;
import com.dprince.entities.InstitutionReceipt;
import com.dprince.entities.Notification;
import com.dprince.entities.enums.AppFeature;
import com.dprince.entities.enums.NotificationActionType;
import com.dprince.entities.utils.AppRepository;
import com.dprince.entities.vos.DonationReport;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReportGenerator {
    private final EntityManager entityManager;
    private final AppRepository appRepository;
    private final EmailService emailService;

    public void generateReport(@NonNull Institution institution){
        Notification notification = Notification.createNotification(null,
                "Auto Report",
                "Starting auto report generation.",
                NotificationActionType.READ,
                null);
        this.appRepository.getNotificationsRepository().save(notification);

        Date now = DateUtils.addDays(-1);
        String today = DateUtils.getDateString(now).split(" ")[0];
        this.appRepository
                .getSubscriptionPlanRepository()
                .findById(institution.getSubscriptionPlan())
                .ifPresent(subscription->{
                    if(subscription.getFeatures().contains(AppFeature.AUTO_REPORT)) {
                        AddressRequestModel reportModel = new AddressRequestModel();
                        reportModel.setInstitution(institution);
                        reportModel.setDuration(DurationGap.SINGLE_DATE);
                        reportModel.setFrom(now);

                        Receiptor receiptor = InstitutionReceipt.getReport(entityManager,
                                this.appRepository,
                                reportModel,
                                institution);
                        List<DonationReport> reportList = receiptor.getReports().getContent();

                        if(!reportList.isEmpty()) {
                            try (Workbook workbook = new XSSFWorkbook()) {
                                // Create a sheet
                                Sheet sheet = workbook.createSheet(institution.getName());

                                Row sheetRow = sheet.createRow(0);
                                this.addHeader(sheetRow);

                                int dataRowIndex = 1;
                                for (DonationReport singleReport : reportList) {
                                    Row dataRow = sheet.createRow(dataRowIndex);
                                    Cell snoCel = dataRow.createCell(0);
                                    snoCel.setCellValue(dataRowIndex);

                                    Cell receiptNo = dataRow.createCell(1);
                                    receiptNo.setCellValue(singleReport.getReceiptNo());

                                    Cell entryDateCell = dataRow.createCell(2);
                                    String entryDate = DateUtils.getDateString(singleReport.getDonationDate());
                                    entryDateCell.setCellValue(entryDate);

                                    Cell partnerCell = dataRow.createCell(3);
                                    partnerCell.setCellValue(singleReport.getMemberCode());

                                    Cell partnerNameCell = dataRow.createCell(4);
                                    partnerNameCell.setCellValue(singleReport.getDonator());

                                    Cell categoryCell = dataRow.createCell(5);
                                    categoryCell.setCellValue(singleReport.getCategory());

                                    Cell paymentModeCell = dataRow.createCell(6);
                                    paymentModeCell.setCellValue(singleReport.getPaymentMode().getDisplayName());

                                    Cell amountCell = dataRow.createCell(7);
                                    amountCell.setCellValue(singleReport.getAmount());
                                    dataRowIndex++;
                                }

                                // Write to ByteArrayOutputStream
                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                workbook.write(out);

                                String messageBody = this.getReportEmail(institution);
                                String attachmentName = "report-" + today + ".xlsx";
                                Map<String, byte[]> attachments = new HashMap<>();
                                attachments.put(attachmentName, out.toByteArray());

                                EmailToSend emailToSend = new EmailToSend();
                                emailToSend.setEmail(institution.getEmail());
                                emailToSend.setEmail(institution.getReportingEmail());
                                emailToSend.setSubject("Report");
                                emailToSend.setByteAttachments(attachments);
                                emailToSend.setBody(messageBody);
                                emailToSend.setLoggedInUser(null);

                                emailService.sendEmail(emailToSend);
                                Notification emailSendNotification = Notification.createNotification(null,
                                        "Auto Report",
                                        "Daily Report \""+attachmentName+"\" sent to \""+institution.getEmail()+"\"",
                                        NotificationActionType.READ,
                                        null);
                                this.appRepository.getNotificationsRepository().save(emailSendNotification);
                            } catch (Exception e) {
                                Notification emailSendNotification = Notification.createNotification(null,
                                        "Auto Report",
                                        "Failed to send Report: "+institution.getName(),
                                        NotificationActionType.READ,
                                        null);
                            }
                        }
                    }
                });
    }

    private void addHeader(Row sheetRow){
        Cell snoCel = sheetRow.createCell(0);
        snoCel.setCellValue("S.No");

        Cell receiptNo = sheetRow.createCell(1);
        receiptNo.setCellValue("Receipt No.");

        Cell entryDateCell = sheetRow.createCell(2);
        entryDateCell.setCellValue("Date");

        Cell partnerCell = sheetRow.createCell(3);
        partnerCell.setCellValue("Partner Code");

        Cell partnerNameCell = sheetRow.createCell(4);
        partnerNameCell.setCellValue("Partner Name");

        Cell categoryCell = sheetRow.createCell(5);
        categoryCell.setCellValue("Category");

        Cell paymentModeCell = sheetRow.createCell(6);
        paymentModeCell.setCellValue("Payment Mode");

        Cell amountCell = sheetRow.createCell(7);
        amountCell.setCellValue("Amount");
    }

    private String getReportEmail(Institution institution){
        return "<div>Dear "+institution.getName()+"</div>" +
                "<div>Please find your report below.</div>";
    }
}
