package com.bezkoder.spring.security.postgresql.payload.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
public class AuthCodeConfirmationRequest {
    @NotBlank
    private String memberEmail;

    @NotBlank
    private String confirmationCode;

    private UUID accessToken;
}
