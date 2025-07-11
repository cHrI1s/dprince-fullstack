package com.dprince.apis.users.vos;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OtpLog {
    private String username;
}
