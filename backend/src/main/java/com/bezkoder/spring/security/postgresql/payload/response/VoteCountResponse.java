package com.bezkoder.spring.security.postgresql.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class VoteCountResponse {
    private int for_count;
    private int against_count;
    private int blank_count;
    private int absent_count;
}
