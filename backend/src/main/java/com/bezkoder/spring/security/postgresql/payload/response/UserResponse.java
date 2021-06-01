package com.bezkoder.spring.security.postgresql.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
    private boolean verifiedApplication;
    private boolean verifiedEmail;
    private boolean activated2FA;
    private boolean adminPriviledge;
    private List<String> roles;
}
