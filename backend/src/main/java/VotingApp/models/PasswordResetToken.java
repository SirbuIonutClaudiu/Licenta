package VotingApp.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.bytebuddy.utility.RandomString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(	name = "password_reset_token",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "member_id")
        })
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String token;

    @Column(name = "member_id")
    private Long memberId;

    private LocalDate expirationDate;

    public PasswordResetToken(Long memberId, String token) {
        this.memberId = memberId;
        this.token = token;
        this.expirationDate = LocalDate.now().plusDays(2);
    }
}
