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
