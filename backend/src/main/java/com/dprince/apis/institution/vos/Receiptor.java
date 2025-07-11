package com.dprince.apis.institution.vos;

import com.dprince.entities.vos.DonationReport;
import com.dprince.entities.vos.PageVO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class Receiptor {
    private Long total;
    private PageVO receipts;
    @JsonIgnore
    private Page<DonationReport> reports;
    public void setReports(Page<DonationReport> reports) {
        this.reports = reports;
        if(reports!=null) {
            PageVO receipts = PageVO.getPageVO(reports);
            this.setReceipts(receipts);
        }
    }


    public void fillCategory(){
        Map<String, DonationReport> theReportsMaps = new HashMap<>();

        // Populate the donation map
        this.getReports().getContent()
                .parallelStream()
                .forEach(donationReport -> {
                    if(!theReportsMaps.containsKey(donationReport.getReceiptNo())){
                        theReportsMaps.put(donationReport.getReceiptNo(), donationReport);
                    } else {
                        DonationReport theDonationReport = theReportsMaps.get(donationReport.getReceiptNo());
                        theDonationReport.addAmount(donationReport.getAmount());
                        theDonationReport.addCategory(donationReport.getCategory());
                        // update the key value
                        theReportsMaps.put(donationReport.getReceiptNo(), theDonationReport);
                    }
                });

        List<DonationReport> newDonations = new ArrayList<>(theReportsMaps.values());

        int requestedSize = this.getReports().getSize();
        int requestPage = this.getReports().getNumber();
        this.setReports(new PageImpl<>(newDonations,
                PageRequest.of(requestPage, requestedSize), newDonations.size()));
    }
}
