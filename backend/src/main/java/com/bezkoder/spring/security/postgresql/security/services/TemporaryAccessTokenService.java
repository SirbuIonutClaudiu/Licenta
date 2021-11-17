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
package com.bezkoder.spring.security.postgresql.security.services;

import com.bezkoder.spring.security.postgresql.models.TemporaryAccessToken;
import com.bezkoder.spring.security.postgresql.repository.TemporaryAccessTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TemporaryAccessTokenService {
    @Autowired
    private TemporaryAccessTokenRepository temporaryAccessTokenRepository;

    public TemporaryAccessToken saveNewAccessToken(Long memberId, String loginRequestPassword) {
        if(temporaryAccessTokenRepository.existsByMemberId(memberId)) {
            TemporaryAccessToken searchAccessToken = temporaryAccessTokenRepository.findByMemberId(memberId);
            temporaryAccessTokenRepository.deleteById(searchAccessToken.getId());
        }
        TemporaryAccessToken newTemporaryAccessToken = new TemporaryAccessToken(memberId, loginRequestPassword);
        temporaryAccessTokenRepository.save(newTemporaryAccessToken);
        return  newTemporaryAccessToken;
    }

    public TemporaryAccessToken findByMemberId(Long memberId) {
        return temporaryAccessTokenRepository.findByMemberId(memberId);
    }

    public void deleteToken(TemporaryAccessToken temporaryAccessToken) {
        temporaryAccessTokenRepository.delete(temporaryAccessToken);
    }
}
