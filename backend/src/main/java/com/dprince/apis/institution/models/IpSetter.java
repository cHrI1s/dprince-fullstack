package com.dprince.apis.institution.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

@Data
public class IpSetter implements Institutionable {
    private Long institutionId;
    private String ip;
}
