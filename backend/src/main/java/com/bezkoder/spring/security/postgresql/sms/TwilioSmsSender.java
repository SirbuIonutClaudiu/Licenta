package com.bezkoder.spring.security.postgresql.sms;

import com.bezkoder.spring.security.postgresql.models.membruSenat;
import com.bezkoder.spring.security.postgresql.repository.membruSenatRepository;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.util.Random;

@AllArgsConstructor
@Service("twilio")
public class TwilioSmsSender implements SmsSender{

    @Autowired
    private membruSenatRepository repo;

    private final TwilioConfiguration twilioConfiguration;

    @Override
    public void sendSms(SmsRequest smsrequest) {
        Long memberId = smsrequest.getId();
        membruSenat member = repo.findById(memberId)
                .orElseThrow(() -> new RuntimeException("No such member !"));
        String phoneNumber = member.getPhoneNumber();
        PhoneNumber to = new PhoneNumber(phoneNumber);
        PhoneNumber from = new PhoneNumber(twilioConfiguration.getPhoneNumber());
        Random rand = new Random();
        String predecessor = "Your UNITBV verification code is : ";
        String code = String.valueOf(rand.nextInt(10000 - 1000) + 1000);
        String message = predecessor + code;
        MessageCreator creator = Message.creator(to, from, message);
        creator.create();
        member.setSmsCode(code);
        repo.save(member);
    }
}
