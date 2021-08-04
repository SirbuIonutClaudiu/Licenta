package com.bezkoder.spring.security.postgresql.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class LoginResponse {
    private UUID token;
    private String phone;
}
