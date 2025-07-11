package com.dprince.entities.models;

import com.dprince.entities.Institution;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Certifier {
    private Institution institution;
    private String ownerCode;
}
