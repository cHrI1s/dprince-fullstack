package com.dprince.configuration.files;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UploadVO {
    private String fileName;
}
