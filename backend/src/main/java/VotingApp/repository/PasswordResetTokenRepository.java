package VotingApp.repository;

import VotingApp.models.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByMemberId(Long id);

    PasswordResetToken findByToken(String resetCode);

    boolean existsByMemberId(Long id);
}
