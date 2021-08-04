package com.bezkoder.spring.security.postgresql.payload.response;

import com.bezkoder.spring.security.postgresql.models.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Data
public class VoteResponse {
    private Long id;
    private String subject;
    private String content;
    private Date startAt;
    private Date endAt;
    private boolean geoRestricted;
    private boolean active;
    private boolean idle;
    private List<Role> roles;
}
