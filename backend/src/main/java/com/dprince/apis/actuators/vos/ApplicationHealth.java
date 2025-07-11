package com.dprince.apis.actuators.vos;


import lombok.Builder;
import lombok.Data;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;

import java.util.Map;

@Builder
@Data
public class ApplicationHealth {
    private Status status;
    private Map<String, Object> info;
    private Map<String, Object> metrics;
}
