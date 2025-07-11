package com.dprince.apis.actuators;

import com.dprince.apis.actuators.vos.ApplicationHealth;
import com.dprince.configuration.auth.Authenticated;
import com.dprince.entities.enums.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@CrossOrigin
@RestController
@RequestMapping("/api/actuator")
public class ActuatorController {
    private final HealthEndpoint healthEndpoint;
    private final MetricsEndpoint metricsEndpoint;
    private final InfoEndpoint infoEndpoint;

    @Authenticated(userTypes = {UserType.SUPER_ADMINISTRATOR})
    @GetMapping("/health")
    public ApplicationHealth health() {
        HealthComponent healthComponent = healthEndpoint.health();
        Map<String, Object> info = this.infoEndpoint.info();
        Map<String, Object> metricsInfo = new HashMap<>();
        MetricsEndpoint.ListNamesResponse listNamesResponse = this.metricsEndpoint.listNames();
        listNamesResponse.getNames()
                .forEach(singleName->{
                    metricsInfo.put(singleName, this.metricsEndpoint.metric(singleName, null));
                });
        return ApplicationHealth.builder()
                .info(info)
                .status(healthComponent.getStatus())
                .metrics(metricsInfo)
                .build();
    }
}