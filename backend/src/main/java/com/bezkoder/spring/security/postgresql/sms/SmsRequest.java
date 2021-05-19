package com.bezkoder.spring.security.postgresql.sms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@ToString
public class SmsRequest {
    @NotBlank
    private final Long id;

    public SmsRequest(Long id) {
        this.id = id;
    }
}
