package com.bezkoder.spring.security.postgresql.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@Entity
@Table(	name = "membru_senat",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "phone_number")
        })
public class membruSenat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    private String institutionalCode;

    @NotBlank
    private String applicationDate;

    @Column(name = "verification_sid")
    private String VerificationSID;

    private String loginLocation;

    private String website;

    private String landline;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "phone_number_2bverified")
    private String phoneNumber2BVerified;

    private boolean disabled = false;

    private boolean verifiedApplication = false;

    private boolean verifiedEmail = false;

    @Column(name="activated_2fa")
    private boolean activated2FA = false;

    @Column(name = "verification_code", length = 64)
    private String verificationCode;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(	name = "user_roles",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public boolean hasAuthorityToVote(Vote vote) {
        if(!this.isVerifiedEmail() || !this.isVerifiedApplication() || this.roles.isEmpty()) {
            return false;
        }
        AtomicBoolean authorization = new AtomicBoolean(false);
        this.roles.forEach(memberRole -> vote.getRoles().forEach(voteRole -> {
            if(memberRole.equals(voteRole)) {
                authorization.set(true);
            }
        }));
        return authorization.get();
    }

    public membruSenat(String name, String institutionalCode, String address, String email, String password,
                       String applicationDate, boolean verifiedApplication, boolean verifiedEmail, boolean activated2FA) {
        this.name = name;
        this.institutionalCode = institutionalCode;
        this.address = address;
        this.email = email;
        this.password = password;
        this.applicationDate = applicationDate;
        this.verifiedApplication = verifiedApplication;
        this.verifiedEmail = verifiedEmail;
        this.activated2FA = activated2FA;
    }
}
