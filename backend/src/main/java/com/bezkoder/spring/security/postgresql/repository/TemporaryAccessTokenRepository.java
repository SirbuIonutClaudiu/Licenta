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
package com.bezkoder.spring.security.postgresql.repository;

import com.bezkoder.spring.security.postgresql.models.TemporaryAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemporaryAccessTokenRepository extends JpaRepository<TemporaryAccessToken, Long> {
    TemporaryAccessToken findByMemberId(Long id);
    boolean existsByMemberId(Long memberId);
}
