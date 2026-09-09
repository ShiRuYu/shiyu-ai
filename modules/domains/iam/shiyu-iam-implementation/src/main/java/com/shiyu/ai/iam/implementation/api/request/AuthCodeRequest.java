package com.shiyu.ai.iam.implementation.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthCodeRequest {
    @NotBlank
    @Size(max = 64)
    private String code;
    private String name;
}

