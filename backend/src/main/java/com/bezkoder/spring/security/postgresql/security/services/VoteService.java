package com.bezkoder.spring.security.postgresql.security.services;

import com.bezkoder.spring.security.postgresql.models.MemberChoice;
import com.bezkoder.spring.security.postgresql.models.Vote;
import com.bezkoder.spring.security.postgresql.models.VoteResult;
import com.bezkoder.spring.security.postgresql.repository.MemberChoiceRepository;
import com.bezkoder.spring.security.postgresql.repository.VoteRepository;
import com.bezkoder.spring.security.postgresql.repository.VoteResultRepository;
import com.bezkoder.spring.security.postgresql.repository.membruSenatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Transactional
public class VoteService {
    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private VoteResultRepository voteResultRepository;

    @Autowired
    private membruSenatRepository membruSenatRepo;

    @Autowired
    private MemberChoiceRepository memberChoiceRepository;

    public List<Vote> returnIdles() {
        List<Vote> result = new ArrayList<Vote>();
        voteRepository.findAll().forEach(vote -> {
            if(vote.isIdle()) {
                result.add(vote);
            }
        });
        return result;
    }

    public void customSave(Vote vote) {
        VoteResult newVoteResult = new VoteResult();
        vote.setVoteResult(newVoteResult);
        newVoteResult.setVote(vote);
        voteRepository.save(vote);
    }
}
