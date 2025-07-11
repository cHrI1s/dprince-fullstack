package com.dprince.apis.institution.vos;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PriestVO {
    private Long id;
    private String fullName;
}
