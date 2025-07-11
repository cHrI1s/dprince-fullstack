package com.dprince.apis.dashboard.vos.parts;

import lombok.Data;

import java.util.Map;


@Data
public class CategorizedStats {
    private Map<String, Long> donations;
    private Map<String, Long> membersByCategory;
}
