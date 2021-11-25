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
