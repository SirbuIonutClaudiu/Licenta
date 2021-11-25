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

Copyright 2020-2021 Sirbu Ionut Claudiu
*/
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
