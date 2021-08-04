package com.bezkoder.spring.security.postgresql.payload.request;

import lombok.Data;

@Data
public class WebsiteRequest {
    private Long id;
    private String website;
}
