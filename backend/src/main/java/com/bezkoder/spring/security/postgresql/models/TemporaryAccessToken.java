package com.bezkoder.spring.security.postgresql.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "temporary_access_token",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "member_id")
})
public class TemporaryAccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "token")
    private UUID token;

    @Column(name = "expiration_date")
    private Date expirationDate;

    @Column(name="login_request_password")
    private String loginRequestPassword;

    public TemporaryAccessToken(Long memberId, String loginRequestPassword) {
        this.memberId = memberId;
        this.loginRequestPassword = loginRequestPassword;
        this.token = UUID.randomUUID();
        Calendar auxiliaryExpirationDate = Calendar.getInstance();
        auxiliaryExpirationDate.setTime(new Date());
        auxiliaryExpirationDate.add(Calendar.HOUR, 1);
        this.expirationDate = auxiliaryExpirationDate.getTime();
    }
}
