package com.dprince.apis.signature;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SignatureModel {
    private Long memberId;
    @NotNull(message = "The institution must be specified.")
    private Long institutionId;

    @NotBlank(message = "The image cannot be empty.")
    private String  signatureImage;
}
