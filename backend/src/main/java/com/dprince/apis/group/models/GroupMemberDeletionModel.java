package com.dprince.apis.group.models;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GroupMemberDeletionModel {
    @NotNull(message = "Group is not specified.")
    private Long groupId;

    @NotNull(message = "Member is not specified.")
    private Long memberId;
}
