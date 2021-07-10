package com.bezkoder.spring.security.postgresql.controllers;

import com.bezkoder.spring.security.postgresql.models.*;
import com.bezkoder.spring.security.postgresql.payload.request.NewVoteRequest;
import com.bezkoder.spring.security.postgresql.payload.request.VoteRequest;
import com.bezkoder.spring.security.postgresql.payload.request.VotesOrganizationRequest;
import com.bezkoder.spring.security.postgresql.payload.request.VotesResultsRequest;
import com.bezkoder.spring.security.postgresql.payload.response.MessageResponse;
import com.bezkoder.spring.security.postgresql.payload.response.VoteCountResponse;
import com.bezkoder.spring.security.postgresql.payload.response.VoteResponse;
import com.bezkoder.spring.security.postgresql.payload.response.VoteSubjectSearchResponse;
import com.bezkoder.spring.security.postgresql.repository.*;
import com.bezkoder.spring.security.postgresql.security.jwt.JwtUtils;
import com.bezkoder.spring.security.postgresql.security.services.MembruSenatService;
import com.bezkoder.spring.security.postgresql.security.services.VoteResultService;
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
import java.util.concurrent.atomic.AtomicBoolean;

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
    membruSenatRepository membruSenatRepo;

    @Autowired
    private final VoteResultRepository voteResultRepository;

    @Autowired
    private final MemberChoiceRepository memberChoiceRepository;

    @Autowired
    private final RoleRepository roleRepository;

    @Autowired
    private final JwtUtils jwtUtils;

    @Autowired
    private final MembruSenatService membruSenatService;

    @Autowired
    private final VoteResultService voteResultService;

    @GetMapping("/search_subjects")
    public ResponseEntity<List<VoteSubjectSearchResponse>> SearchSubjects(@RequestHeader("Authorization") String auth) {
        List<VoteSubjectSearchResponse> result = new ArrayList<>();
        List<Role> roles = getRolesFromAuthentication(auth);
        List<VoteResponse> allVoteResponses = VoteResponseForUser(roles);
        assert allVoteResponses != null;
        allVoteResponses.forEach(voteResponse -> {
            result.add(new VoteSubjectSearchResponse(voteResponse.getId(), voteResponse.getSubject()));
        });
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/all_votes")
    public ResponseEntity<List<VoteResponse>> getAllVotes(@RequestHeader("Authorization") String auth,
                                          @Valid @RequestBody VotesOrganizationRequest votesOrganizationRequest) {
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not inserted !"));
        List<Role> roles = getRolesFromAuthentication(auth);
        List<VoteResponse> allVoteResponses = VoteResponseForUser(roles);
        if(votesOrganizationRequest.isRoleRestriction() && !roles.contains(adminRole)) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(votesOrganizationRequest.SortVotesByRequest(allVoteResponses), HttpStatus.OK);
    }

    private List<VoteResponse> VoteResponseForUser(List<Role> roles) {
        List<VoteResponse> result = new ArrayList<>();
        voteRepository.findAll().forEach(vote -> {
            result.add(VoteToVoteResponse(vote));
        });
        if(roleRepository.findByName(ERole.ROLE_ADMIN).isPresent()) {
            if(roles.contains(roleRepository.findByName(ERole.ROLE_ADMIN).get())) {
                return result;
            }
            else {
                result.removeIf(currentVote -> (Collections.disjoint(currentVote.getRoles(), roles)));
                return result;
            }
        }
        return null;
    }

    private List<Role> getRolesFromAuthentication(String auth) {
        String token = auth.substring(7,auth.length());
        String email = jwtUtils.getEmailFromJwtToken(token);
        membruSenat member = membruSenatService.findMemberByEmail(email);
        return new ArrayList<>(member.getRoles());
    }

    private membruSenat getMemberFromAuthentication(String auth) {
        String token = auth.substring(7,auth.length());
        String email = jwtUtils.getEmailFromJwtToken(token);
        return membruSenatService.findMemberByEmail(email);
    }

    @GetMapping("/is_not_first/{id}")
    public ResponseEntity<?> isNotFirst(@PathVariable("id") Long id) {
        Vote vote = voteRepository.findById(id-1)
                .orElseThrow(() -> new RuntimeException("Vote not found !"));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/is_not_last/{id}")
    public ResponseEntity<?> isNotLast(@PathVariable("id") Long id) {
        Vote vote = voteRepository.findById(id+1)
                .orElseThrow(() -> new RuntimeException("Vote not found !"));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<VoteResponse> getVoteById(@PathVariable("id") Long id) {
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vote not found !"));
        return new ResponseEntity<>(VoteToVoteResponse(vote), HttpStatus.OK);
    }

    private VoteResponse VoteToVoteResponse(Vote vote) {
        return new VoteResponse( vote.getId(),
                                 vote.getSubject(),
                                 vote.getContent(),
                                 vote.getStartAt(),
                                 vote.getEndAt(),
                                 vote.isGeoRestricted(),
                                 vote.isActive(),
                                 vote.isIdle(),
                                 vote.getRoles() );
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
        voteService.customSave(newVote);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/vote")
    public ResponseEntity<?> Vote(@RequestHeader("Authorization") String auth, @Valid @RequestBody VoteRequest voteRequest) {
        Vote vote = voteRepository.findById(voteRequest.getVote_id())
                .orElseThrow(() -> new RuntimeException("Vote not found !"));
        if(!vote.isIdle()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Vote closed !"));
        }
        if(!vote.isActive()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Vote is not open at this moment !"));
        }
        membruSenat member = getMemberFromAuthentication(auth);
        if(!member.hasAuthorityToVote(vote)){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Member does not have authority to vote !"));
        }
        VoteResult voteResult = vote.getVoteResult();
        if(voteResult.userVoted(member)) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Member already voted !"));
        }
        MemberChoice newVote = new MemberChoice(member.getId(), voteRequest.getChoice());
        voteResult.getMemberChoices().add(newVote);
        memberChoiceRepository.save(newVote);
        voteResultRepository.save(voteResult);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/vote_result/{id}")
    public ResponseEntity<VoteCountResponse> VoteResult(@PathVariable("id") Long id) {
        Vote vote = voteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vote not found !"));
        VoteResult voteResult = vote.getVoteResult();
        VoteCountResponse voteCountResponse = new VoteCountResponse( voteResultService.getVoteCount(voteResult.getId(), "for"),
                                                                     voteResultService.getVoteCount(voteResult.getId(), "against"),
                                                                     voteResultService.getVoteCount(voteResult.getId(), "blank"),
                                                                     voteResultService.getVoteCount(voteResult.getId(), "absent") );
        return new ResponseEntity<>(voteCountResponse, HttpStatus.OK);
    }

    @PostMapping("/votes_results")
    public ResponseEntity<List<VoteCountResponse>> VoteResult(@Valid @RequestBody VotesResultsRequest votesResultsRequest) {
        List<VoteCountResponse> result = new ArrayList<>();
        votesResultsRequest.getVotesIds().forEach(voteId -> {
            Vote vote = voteRepository.findById(voteId)
                    .orElseThrow(() -> new RuntimeException("Vote not found !"));
            VoteResult voteResult = vote.getVoteResult();
            VoteCountResponse voteCountResponse = new VoteCountResponse( voteResultService.getVoteCount(voteResult.getId(), "for"),
                    voteResultService.getVoteCount(voteResult.getId(), "against"),
                    voteResultService.getVoteCount(voteResult.getId(), "blank"),
                    voteResultService.getVoteCount(voteResult.getId(), "absent") );
            result.add(voteCountResponse);
        });
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/user_voted/{vote_id}")
    public ResponseEntity<?> userVoted(@RequestHeader("Authorization") String auth, @PathVariable("vote_id") Long vote_id) {
        membruSenat member = getMemberFromAuthentication(auth);
        Vote vote = voteRepository.findById(vote_id)
                .orElseThrow(() -> new RuntimeException("Vote not found !"));
        VoteResult voteResult = vote.getVoteResult();
        return new ResponseEntity<>(voteResult.userVoted(member), HttpStatus.OK);
    }

    @Async
    @Scheduled(fixedRate = 1000)
    public void scheduleFixedRateTaskAsync() throws InterruptedException {
        voteService.returnIdles().forEach(vote -> {
            Date now = new Date();
            Calendar startAt = Calendar.getInstance();
            startAt.setTime(vote.getStartAt());
            Calendar endAt = Calendar.getInstance();
            endAt.setTime(vote.getEndAt());
            if(now.after(endAt.getTime())) {
                this.endVote(vote);
            }
            else if(now.after(startAt.getTime()) && now.before(endAt.getTime()) && !vote.isActive()) {
                this.startVote(vote);
            }
        });
    }

    private void endVote(Vote vote) {
        vote.endVote();
        voteRepository.save(vote);
        VoteResult voteResult = vote.getVoteResult();
        membruSenatService.returnAll().forEach(membruSenat -> {
            if(membruSenat.hasAuthorityToVote(vote) && !voteResult.userVoted(membruSenat)) {
                MemberChoice newVote = new MemberChoice(membruSenat.getId(), "absent");
                voteResult.getMemberChoices().add(newVote);
                memberChoiceRepository.save(newVote);
            }
        });
        voteResultRepository.save(voteResult);
    }

    private void startVote(Vote vote) {
        vote.startVote();
        voteRepository.save(vote);
    }
}
