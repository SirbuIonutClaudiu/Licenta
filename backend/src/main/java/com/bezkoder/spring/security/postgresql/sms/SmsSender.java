package com.bezkoder.spring.security.postgresql.sms;

public interface SmsSender {

    void sendSms(SmsRequest smsrequest);
}
