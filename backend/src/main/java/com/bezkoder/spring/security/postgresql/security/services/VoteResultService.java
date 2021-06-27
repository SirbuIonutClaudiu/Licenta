package com.bezkoder.spring.security.postgresql.security.services;

import com.bezkoder.spring.security.postgresql.repository.VoteResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class VoteResultService {
    @Autowired
    private VoteResultRepository voteResultRepository;

    public int getVoteCount(Long id, String vote) {
        AtomicInteger counter = new AtomicInteger();
        voteResultRepository.findAll().forEach(voteResult -> {
            if(voteResult.getId().equals(id)) {
                voteResult.getMemberChoices().forEach(memberChoice -> {
                    if(memberChoice.getChoice().equals(vote)) {
                        counter.getAndIncrement();
                    }
                });
            }
        });
        return counter.get();
    }
}
