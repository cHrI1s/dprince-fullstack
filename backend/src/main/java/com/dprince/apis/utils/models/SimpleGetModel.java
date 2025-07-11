package com.dprince.apis.utils.models;

import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;



@Data
public class SimpleGetModel implements Institutionable {
    private Long institutionId;
}
