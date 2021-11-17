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
