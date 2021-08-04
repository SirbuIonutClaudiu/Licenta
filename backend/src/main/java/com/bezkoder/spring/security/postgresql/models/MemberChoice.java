package com.bezkoder.spring.security.postgresql.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "member_choice")
public class MemberChoice {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    private Long member_id;

    private String choice;

    public MemberChoice(Long member_id, String choice) {
        this.member_id = member_id;
        this.choice = choice;
    }
}
