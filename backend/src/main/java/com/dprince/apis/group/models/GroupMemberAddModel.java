package com.dprince.apis.group.models;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class GroupMemberAddModel {
    @NotNull(message = "Please specify the member.")
    private Long memberId;

    @NotNull(message = "Groups should be specified.")
    @Size(min = 1, message = "The minimum groups allowed is 1.")
    private List<Long> groupsIds;
}
