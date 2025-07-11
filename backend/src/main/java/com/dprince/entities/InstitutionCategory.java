package com.dprince.entities;

import com.dprince.apis.misc.category.models.validators.CategoryCreationValidator;
import com.dprince.apis.misc.category.models.validators.CategoryUpdateValidator;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@FieldNameConstants
@Data
@Entity
@Table(name = "categories")
public class InstitutionCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    private Long institutionId;

    @NotBlank(message = "The name must not be empty.", groups = {
            CategoryCreationValidator.class,
            CategoryUpdateValidator.class
    })
    @Column(nullable = false)
    private String name;
    public void setName(@NotBlank(message = "The name must not be empty.", groups = {
            CategoryCreationValidator.class,
            CategoryUpdateValidator.class
    }) String name) {
        if(!StringUtils.isEmpty(name)) name = name.toUpperCase();
        this.name = name;
    }
}
