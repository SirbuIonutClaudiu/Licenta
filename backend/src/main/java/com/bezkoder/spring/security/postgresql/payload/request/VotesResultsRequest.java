package com.bezkoder.spring.security.postgresql.payload.request;

import lombok.Data;
import java.util.List;

@Data
public class VotesResultsRequest {
    private List<Long> votesIds;
}
