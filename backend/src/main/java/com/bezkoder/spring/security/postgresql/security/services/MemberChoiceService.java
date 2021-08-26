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
