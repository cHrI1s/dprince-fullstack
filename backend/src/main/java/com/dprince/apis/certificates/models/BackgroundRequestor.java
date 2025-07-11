package com.dprince.apis.certificates.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

@Data
public class BackgroundRequestor implements Institutionable {
    private Long institutionId;
}
