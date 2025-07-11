package com.dprince.apis.institution.models;

import com.dprince.entities.enums.CommunicationWay;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ReceiptAcknowledger {
    @NotNull(message = "Receipt not specified.")
    private Long receiptId;

    @NotNull(message = "Communication way not Specified.")
    @Size(min = 1, message = "Minimum number of communication ways must be 1")
    private List<CommunicationWay> communicationWays;
}
