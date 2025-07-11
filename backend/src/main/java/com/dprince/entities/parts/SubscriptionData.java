package com.dprince.entities.parts;

import com.dprince.entities.enums.Subscription;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class SubscriptionData {
    private Subscription subscription;
    private Date deadline;
}
