package com.bezkoder.spring.security.postgresql.payload.request;

import lombok.Data;

@Data
public class VoteRequest {
    private Long vote_id;
    private String choice;
}
