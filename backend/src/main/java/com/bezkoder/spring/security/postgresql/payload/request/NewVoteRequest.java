package com.bezkoder.spring.security.postgresql.payload.request;

import lombok.Data;
import java.util.Date;

@Data
public class NewVoteRequest {
    private String subject;

    private String content;

    private Date startAt;

    private int duration;
}
