package com.bezkoder.spring.security.postgresql.security.services;

import com.bezkoder.spring.security.postgresql.models.Vote;
import com.bezkoder.spring.security.postgresql.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class VoteService {
    @Autowired
    private VoteRepository voteRepository;

    public List<Vote> returnIdles() {
        List<Vote> result = new ArrayList<Vote>();
        voteRepository.findAll().forEach(vote -> {
            if(vote.isIdle()) {
                result.add(vote);
            }
        });
        return result;
    }
}
