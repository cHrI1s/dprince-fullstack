package com.dprince.apis.misc.category.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import com.dprince.entities.InstitutionCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class CategoryCreator implements Institutionable {
    private Long institutionId;

    @Valid
    @Size(min = 1, message="You should provide at least one title.")
    private List<InstitutionCategoryModel> categories;

    @JsonIgnore
    private List<InstitutionCategory> originalCategories;
}
