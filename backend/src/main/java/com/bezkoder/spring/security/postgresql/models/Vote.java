package com.bezkoder.spring.security.postgresql.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.Date;

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

    private boolean active;

    private boolean idle;

    public void startVote() {
        this.active = true;
    }

    public void endVote() {
        this.active = false;
        this.idle = false;
    }

    public Vote(String subject, String content, Date startAt, Date endAt) {
        this.subject = subject;
        this.content = content;
        this.startAt = startAt;
        this.endAt = endAt;
        this.active = false;
        this.idle = true;
    }
}
