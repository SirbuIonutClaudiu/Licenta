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
*/
package com.bezkoder.spring.security.postgresql.security.services;

import com.bezkoder.spring.security.postgresql.repository.MemberChoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberChoiceService {
    @Autowired
    private MemberChoiceRepository memberChoiceRepository;

    public int getTotalNumberOfForVotes(Long memberId) {
        return (int) memberChoiceRepository.findAll().stream().filter(memberChoice ->
                memberChoice.getMember_id().equals(memberId) && memberChoice.getChoice().equals("for")).count();
    }

    public int getTotalNumberOfAgainstVotes(Long memberId) {
        return (int) memberChoiceRepository.findAll().stream().filter(memberChoice ->
                memberChoice.getMember_id().equals(memberId) && memberChoice.getChoice().equals("against")).count();
    }

    public int getTotalNumberOfBlankVotes(Long memberId) {
        return (int) memberChoiceRepository.findAll().stream().filter(memberChoice ->
                memberChoice.getMember_id().equals(memberId) && memberChoice.getChoice().equals("blank")).count();
    }

    public int getTotalNumberOfAbsentVotes(Long memberId) {
        return (int) memberChoiceRepository.findAll().stream().filter(memberChoice ->
                memberChoice.getMember_id().equals(memberId) && memberChoice.getChoice().equals("absent")).count();
    }
}
