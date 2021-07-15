package com.bezkoder.spring.security.postgresql.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vote")
public class Vote {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    private String content;

    private Date startAt;

    private Date endAt;

    @Column(name = "geo_restricted")
    private boolean geoRestricted;

    @Column(name = "email_reminder")
    private boolean emailReminder;

    private boolean active;

    private boolean idle;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(	name = "vote_roles",
            joinColumns = @JoinColumn(name = "vote_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;

    @OneToOne(fetch = FetchType.LAZY,
            cascade =  CascadeType.ALL,
            mappedBy = "vote")
    private VoteResult voteResult;

    public void startVote() {
        this.active = true;
    }

    public void endVote() {
        this.active = false;
        this.idle = false;
    }

    public Vote(String subject, String content, Date startAt, Date endAt, boolean geoRestricted, boolean emailReminder, List<Role> roles) {
        this.subject = subject;
        this.content = content;
        this.startAt = startAt;
        this.endAt = endAt;
        this.geoRestricted = geoRestricted;
        this.emailReminder = emailReminder;
        this.active = false;
        this.idle = true;
        this.roles = roles;
    }
}
