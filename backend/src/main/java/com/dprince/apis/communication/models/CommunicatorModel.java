package com.dprince.apis.communication.models;

import com.dprince.apis.utils.DateUtils;
import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.enums.CommunicationWay;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class CommunicatorModel implements Institutionable {
    private Long id;

    private Long institutionId;

    private List<Long> destination;

    private Long destinationGroup;

    @NotNull(message = "Communication way not specified.")
    private CommunicationWay way;

    private String message;

    private String subject = "No-Subject";

    private Long templateId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date sentDate = DateUtils.getNowDateTime();


    private List<String> recipient;

    public void checkAll(){
        if(this.getWay().equals(CommunicationWay.MAIL) && StringUtils.isEmpty(this.getSubject())){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "Subject is not defined.");
        }
        this.checkDestination();
    }
    private void checkDestination(){
        if((this.getDestination()==null || this.getDestination().isEmpty())
                && this.getDestinationGroup()==null &&
                (this.getRecipient()==null || this.getRecipient().isEmpty())){
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Please specify the destination of the message.");
        }
    }
}
