package com.dprince.apis.church.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.ChurchContribution;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ContributionMaker implements Institutionable {
    @Size(min = 1, message = "At least one contribution should be provided.")
    private List<ChurchContribution> contributions;

    private Long institutionId;
}
