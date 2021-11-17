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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vote_result")
public class VoteResult {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(	name = "vote_result_members",
            joinColumns = @JoinColumn(name = "result_id"),
            inverseJoinColumns = @JoinColumn(name = "choice_id"))
    private Set<MemberChoice> memberChoices = new HashSet<>();

    public boolean userVoted(membruSenat member) {
        AtomicBoolean voted = new AtomicBoolean(false);
        this.getMemberChoices().forEach(memberChoice -> {
            if(memberChoice.getMember_id().equals(member.getId()) && !memberChoice.getChoice().equals("absent")) {
                voted.set(true);
            }
        });
        return voted.get();
    }
}