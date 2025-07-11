package com.dprince.apis.institution.models.files;

import com.dprince.entities.enums.CertificateType;
import com.dprince.apis.utils.behaviors.Institutionable;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CertificateImporter implements Institutionable {
    private Long institutionId;

    @NotBlank(message = "No File specified!")
    private String fileName;

    @NotNull(message = "The type of the certificate is not defined!")
    private CertificateType type;
}
