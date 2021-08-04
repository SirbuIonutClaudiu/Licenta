package com.bezkoder.spring.security.postgresql.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendPhoneVerificationRequest {
    @NotBlank
    private String memberEmail;

    private UUID accessToken;
}
