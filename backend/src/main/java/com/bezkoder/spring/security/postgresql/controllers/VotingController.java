package com.bezkoder.spring.security.postgresql.controllers;

import com.bezkoder.spring.security.postgresql.models.ERole;
import com.bezkoder.spring.security.postgresql.models.Role;
import com.bezkoder.spring.security.postgresql.models.Vote;
import com.bezkoder.spring.security.postgresql.payload.request.NewVoteRequest;
import com.bezkoder.spring.security.postgresql.payload.response.VoteResponse;
import com.bezkoder.spring.security.postgresql.repository.RoleRepository;
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
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@EnableAsync
@EnableScheduling
@AllArgsConstructor
@RestController
@RequestMapping("/api/voting")
public class VotingController {

    @Autowired
    private final VoteRepository voteRepository;

    @Autowired
    private final VoteService voteService;

    @Autowired
    private final RoleRepository roleRepository;

    @GetMapping("/find/{id}")
    public ResponseEntity<VoteResponse> getVoteById(@PathVariable("id") Long id) {
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vote not found !"));
        VoteResponse voteResponse = voteToVoteResponse(vote);
        return new ResponseEntity<>(voteResponse, HttpStatus.OK);
    }

    private VoteResponse voteToVoteResponse(Vote vote) {
        return new VoteResponse( vote.getId(),
                                 vote.getSubject(),
                                 vote.getContent(),
                                 vote.isGeoRestricted(),
                                 vote.isActive(),
                                 vote.isIdle(),
                                 vote.getRoles());
    }

    @PostMapping("/new_vote")
    public ResponseEntity<?> newVote(@Valid @RequestBody NewVoteRequest newVoteRequest) throws ParseException {
        List<Role> roles = new ArrayList<Role>();
        for(ERole role : newVoteRequest.getRoles()) {
            Role newRole = roleRepository.findByName(role)
                    .orElseThrow(() -> new RuntimeException("Role does not exist !"));
            roles.add(newRole);
        }
        int dateLength = newVoteRequest.getStartAt().length();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy, HH:mm:ss");
        Date startAt = formatter.parse(newVoteRequest.getStartAt());
        Calendar startAt_aux = Calendar.getInstance();
        startAt_aux.setTime(startAt);
        if(newVoteRequest.getStartAt().substring(dateLength-2, dateLength).equals("PM")) {
            startAt_aux.add(Calendar.HOUR, 12);
            startAt = startAt_aux.getTime();
        }
        Calendar endAt = Calendar.getInstance();
        endAt.setTime(startAt);
        switch(newVoteRequest.getDuration()) {
            case 1: endAt.add(Calendar.SECOND, 30); break;
            case 2: endAt.add(Calendar.MINUTE, 1); break;
            case 3: endAt.add(Calendar.MINUTE, 2); break;
            case 4: endAt.add(Calendar.MINUTE, 3); break;
            case 5: endAt.add(Calendar.MINUTE, 4); break;
            case 6: endAt.add(Calendar.MINUTE, 5); break;
        }
        Vote newVote = new Vote(newVoteRequest.getSubject(), newVoteRequest.getContent(),
                startAt, endAt.getTime(), newVoteRequest.isGeoRestricted(), roles);
        voteRepository.save(newVote);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Async
    @Scheduled(fixedRate = 1000)
    public void scheduleFixedRateTaskAsync() throws InterruptedException {
        voteService.returnIdles().forEach(vote -> {
            Date now = new Date();
            if(now.after(vote.getEndAt())) {
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
