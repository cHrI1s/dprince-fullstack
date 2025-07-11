package com.dprince.apis.communication.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

@Data
public class TemplateModel  implements Institutionable {
    private Long institutionId;
}
