/*
    This file is part of UnitbVoting application.

    UnitbVoting is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UnitbVoting is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UnitbVoting. If not, see <https://www.gnu.org/licenses/>.

Copyright 2020-2021 Sirbu Ionut Claudiu
*/
package com.bezkoder.spring.security.postgresql.security.services;

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
