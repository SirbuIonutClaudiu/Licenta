/*
    This file is part of UnitbVoting application.

    UnitbVoting is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UnitbVoting is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UnitbVoting. If not, see <https://www.gnu.org/licenses/>.
*/
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

    @Column(columnDefinition="TEXT")
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
