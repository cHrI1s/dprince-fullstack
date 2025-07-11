package com.dprince.apis.utils.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ChurchBelongingListQuery extends ListQuery implements Institutionable {
    private Long institutionId;
    @JsonIgnore
    private List<Long> institutionsIds;
    private Boolean published;
}
