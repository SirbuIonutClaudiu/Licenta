package com.bezkoder.spring.security.postgresql.payload.request;

import com.bezkoder.spring.security.postgresql.models.ERole;
import com.bezkoder.spring.security.postgresql.models.Role;
import com.bezkoder.spring.security.postgresql.payload.response.VoteResponse;
import com.bezkoder.spring.security.postgresql.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import lombok.Data;

@Data
public class VotesOrganizationRequest {
    @Autowired
    private RoleRepository roleRepository;

    private int page;
    private int perPage;
    private String sortParameter;
    private String sortDirection;
    private boolean enableGeorestriction;
    private boolean geoRestrictedOption;
    private String status;
    private boolean roleRestriction;
    private List<ERole> Eroles;

    public VotesOrganizationRequest(int page, int perPage, String sortParameter, String sortDirection, boolean enableGeorestriction,
                                    boolean geoRestrictedOption, String status, boolean roleRestriction, List<ERole> eroles) {
        this.page = page;
        this.perPage = perPage;
        this.sortParameter = sortParameter;
        this.sortDirection = sortDirection;
        this.enableGeorestriction = enableGeorestriction;
        this.geoRestrictedOption = geoRestrictedOption;
        this.status = status;
        this.roleRestriction = roleRestriction;
        Eroles = eroles;
    }

    public List<VoteResponse> SortVotesByRequest(List<VoteResponse> allVotes) {
        List<VoteResponse> result;
        result = SortVotesBy(VotesPerPage(ShowGeoRestriction(ShowStatus(allVotes))));
        return (this.roleRestriction ? ShowRoles(result) : result);
    }

    public List<VoteResponse> VotesPerPage(List<VoteResponse> allVotes) {
        return allVotes.subList(this.perPage*this.page, Math.min(allVotes.size(), (this.perPage * (this.page + 1))));
    }

    private List<Role> getRolesFromERoles() {
        List<Role> result = new ArrayList<>();
        this.getEroles().forEach(eRole -> roleRepository.findByName(eRole).ifPresent(result::add));
        return result;
    }

    public List<VoteResponse> ShowRoles(List<VoteResponse> allVotes) {
        List<Role> roles = getRolesFromERoles();
        allVotes.removeIf(currentResponse -> (!Collections.disjoint(currentResponse.getRoles(), roles)));
        return  allVotes;
    }

    public List<VoteResponse> ShowStatus(List<VoteResponse> allVotes) {
        allVotes.removeIf(currentResponse -> (this.status.equals("active") && !currentResponse.isActive()) ||
                (this.status.equals("idle") && !currentResponse.isIdle()) ||
                (this.status.equals("ended") && currentResponse.isIdle()) );
        return  allVotes;
    }

    public List<VoteResponse> ShowGeoRestriction(List<VoteResponse> allVotes) {
        allVotes.removeIf(currentResponse -> (this.enableGeorestriction && (this.geoRestrictedOption != currentResponse.isGeoRestricted())));
        return  allVotes;
    }

    public List<VoteResponse> SortVotesBy(List<VoteResponse> allVotes) {
        if(this.sortParameter.equals("start")) {
            if(this.sortDirection.equals("asc")) {
                allVotes.sort(Comparator.comparing(VoteResponse::getStartAt));
            }
            else {
                allVotes.sort(Comparator.comparing(VoteResponse::getStartAt).reversed());
            }
        }
        else {
            if(this.sortDirection.equals("asc")) {
                allVotes.sort(Comparator.comparing(VoteResponse::getEndAt));
            }
            else {
                allVotes.sort(Comparator.comparing(VoteResponse::getEndAt).reversed());
            }
        }
        return allVotes;
    }
}

