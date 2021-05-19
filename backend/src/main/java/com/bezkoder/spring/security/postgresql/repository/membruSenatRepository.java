package com.bezkoder.spring.security.postgresql.repository;

import com.bezkoder.spring.security.postgresql.models.membruSenat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface membruSenatRepository extends JpaRepository<membruSenat, Long> {

  Optional<membruSenat> findByEmail(String email);

  membruSenat findByVerificationCode(String code);

  Optional<membruSenat> findById(Long id);

  void deleteById(Long id);

  Boolean existsByEmail(String email);
}
