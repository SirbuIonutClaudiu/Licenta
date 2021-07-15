package com.bezkoder.spring.security.postgresql.payload.request;

import com.bezkoder.spring.security.postgresql.models.ERole;
import lombok.Data;

@Data
public class NewVoteRequest {
    private String subject;
    private String content;
    private String startAt;
    private int duration;
    private boolean geoRestricted;
    private boolean emailReminder;
    private ERole[] roles;
}
