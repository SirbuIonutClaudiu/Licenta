package com.bezkoder.spring.security.postgresql.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

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

    private Date expirationDate;

    public PasswordResetToken(Long memberId, String token) {
        this.memberId = memberId;
        this.token = token;
        Calendar expirationDateCalendar = Calendar.getInstance();
        expirationDateCalendar.setTime(new Date());
        expirationDateCalendar.add(Calendar.HOUR, 24);
        this.expirationDate = expirationDateCalendar.getTime();
    }
}
