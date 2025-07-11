package com.dprince.apis.misc.category.models;

import com.dprince.apis.misc.category.models.validators.CategoryCreationValidator;
import com.dprince.apis.misc.category.models.validators.CategoryUpdateValidator;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.validation.constraints.NotBlank;

@Data
public class InstitutionCategoryModel {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @NotBlank(message = "The name must not be empty.", groups = {
            CategoryCreationValidator.class,
            CategoryUpdateValidator.class
    })
    private String name;
}
