package com.dprince.apis.group.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.InstitutionGroup;
import lombok.Data;


import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
public class GroupCreator implements Institutionable {

    @Valid
    @Size(min = 1, message="You should provide at least one group.")
    private List<InstitutionGroup> groups;
    private Long institutionId;


    // Adds a new title
    public void addGroup(InstitutionGroup group){
        if(this.getGroups()==null) this.groups = new ArrayList<>();
        this.groups.add(group);

    }
}
