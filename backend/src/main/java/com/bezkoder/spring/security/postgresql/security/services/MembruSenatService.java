package com.bezkoder.spring.security.postgresql.security.services;

import com.bezkoder.spring.security.postgresql.models.membruSenat;
import com.bezkoder.spring.security.postgresql.repository.membruSenatRepository;
import com.bezkoder.spring.security.postgresql.security.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class MembruSenatService {

    private final membruSenatRepository membruRepo;

    @Autowired
    public MembruSenatService(membruSenatRepository membruRepo) {
        this.membruRepo = membruRepo;
    }

    public membruSenat findMemberById(Long id) {
        return membruRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Member by id " + id + " was not found"));
    }

    public membruSenat findMemberByEmail(String email) {
        return membruRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Member by email " + email + " was not found"));
    }

    public void deleteMemberById(Long id){
        membruRepo.deleteById(id);
    }

    public List<membruSenat> returnAll() {
        return membruRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }
}
