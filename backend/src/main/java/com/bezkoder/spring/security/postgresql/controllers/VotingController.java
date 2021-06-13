package com.bezkoder.spring.security.postgresql.controllers;

import com.bezkoder.spring.security.postgresql.models.Vote;
import com.bezkoder.spring.security.postgresql.payload.request.NewVoteRequest;
import com.bezkoder.spring.security.postgresql.repository.VoteRepository;
import com.bezkoder.spring.security.postgresql.security.services.VoteService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Calendar;
import java.util.Date;

@EnableAsync
@EnableScheduling
@AllArgsConstructor
@RestController
@RequestMapping("/api/voting")
public class VotingController {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private VoteService voteService;

    @PostMapping("/add_vote")
    public ResponseEntity<?> addVote() {
        String subject = "subj";
        String content = "cont";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 5);
        Date startAt = calendar.getTime();
        calendar.add(Calendar.SECOND, 8);
        Date endAt = calendar.getTime();
        Vote newVote = new Vote(subject, content, startAt, endAt);
        voteRepository.save(newVote);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/new_vote")
    public ResponseEntity<?> newVote(@Valid @RequestBody NewVoteRequest newVoteRequest) {
        Calendar endAt = Calendar.getInstance();
        endAt.setTime(newVoteRequest.getStartAt());
        switch(newVoteRequest.getDuration()) {
            case 1: endAt.add(Calendar.SECOND, 30);
            case 2: endAt.add(Calendar.MINUTE, 1);
            case 3: endAt.add(Calendar.MINUTE, 2);
            case 4: endAt.add(Calendar.MINUTE, 3);
            case 5: endAt.add(Calendar.MINUTE, 4);
            case 6: endAt.add(Calendar.MINUTE, 5);
        }
        System.out.println(newVoteRequest);
        System.out.println(endAt);
        Vote newVote = new Vote(newVoteRequest.getSubject(), newVoteRequest.getContent(),
                newVoteRequest.getStartAt(), endAt.getTime());
        voteRepository.save(newVote);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //@Async
    //@Scheduled(fixedRate = 1000)
    public void scheduleFixedRateTaskAsync() throws InterruptedException {
        voteService.returnIdles().forEach(vote -> {
            Date now = new Date();
            if(now.after(vote.getEndAt()) && vote.isActive()) {
                vote.endVote();
                voteRepository.save(vote);
            }
            else if(now.after(vote.getStartAt()) && now.before(vote.getEndAt()) && !vote.isActive()) {
                vote.startVote();
                voteRepository.save(vote);
            }
        });
    }
}
