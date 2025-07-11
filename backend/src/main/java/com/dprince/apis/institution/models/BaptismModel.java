package com.dprince.apis.institution.models;

import com.dprince.apis.institution.models.validations.LocalPastorBaptist;
import com.dprince.apis.institution.models.validations.GuestPastorBaptist;
import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class BaptismModel implements Institutionable {
    @NotNull(message = "The member to baptize should be defined!",
            groups = {
                LocalPastorBaptist.class,
                GuestPastorBaptist.class
            })
    private Long baptismalCandidateId;

    @NotNull(message = "The Date of baptism cannot be empty.",
            groups = {
                LocalPastorBaptist.class,
                GuestPastorBaptist.class
            })
    private Date dateOfBaptism;

    private Long institutionId;

    @NotNull(message = "Please specify the baptist",
            groups = {
                LocalPastorBaptist.class
            })
    private Long baptist;

    @NotNull(message = "Please specify the baptist",
            groups = {
                    GuestPastorBaptist.class
            })
    private Long guestBaptist;

    private boolean localBaptizer;
}
