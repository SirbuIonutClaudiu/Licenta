package com.bezkoder.spring.security.postgresql.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@AllArgsConstructor
@Data
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String address;
    private String institutionalCode;
    private String applicationDate;
    private String loginLocation;
    private String website;
    private String landline;
    private String phoneNumber;
    private boolean disabled;
    private boolean verifiedApplication;
    private boolean verifiedEmail;
    private boolean activated2FA;
    private List<String> roles;
}
