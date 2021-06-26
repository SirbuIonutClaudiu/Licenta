package com.bezkoder.spring.security.postgresql.repository;

import com.bezkoder.spring.security.postgresql.models.MemberChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface MemberChoiceRepository extends JpaRepository<MemberChoice, UUID> {
}
