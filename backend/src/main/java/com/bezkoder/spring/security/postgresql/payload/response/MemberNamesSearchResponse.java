package com.bezkoder.spring.security.postgresql.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberNamesSearchResponse {
    private Long id;
    private String name;
}
