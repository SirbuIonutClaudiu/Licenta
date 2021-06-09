package com.bezkoder.spring.security.postgresql.payload.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class PasswordRequest {
    private Long id;

    @NotBlank
    private String password;
}
