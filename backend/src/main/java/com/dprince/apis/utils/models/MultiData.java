package com.dprince.apis.utils.models;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class MultiData {
    @NotNull(message = "Please specify items")
    @Size(message = "Items must not be lower than 1", min = 1)
    private List<Long> itemsIds;
}
