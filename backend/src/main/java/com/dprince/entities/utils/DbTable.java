package com.dprince.entities.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public abstract class DbTable {
    @JsonIgnore
    private AppRepository repository;
}
