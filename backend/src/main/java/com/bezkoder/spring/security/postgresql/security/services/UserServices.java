package com.bezkoder.spring.security.postgresql.security.services;

import com.bezkoder.spring.security.postgresql.models.PasswordResetToken;
import com.bezkoder.spring.security.postgresql.models.membruSenat;
import com.bezkoder.spring.security.postgresql.repository.PasswordResetTokenRepository;
import com.bezkoder.spring.security.postgresql.repository.membruSenatRepository;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Date;

@Service
public class UserServices {

    @Autowired
    private membruSenatRepository repo;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    public void register(membruSenat member, String siteURL)
            throws UnsupportedEncodingException, MessagingException {

        String randomCode = RandomString.make(64);
        member.setVerificationCode(randomCode);
        member.setVerifiedEmail(false);

        sendVerificationEmail(member, siteURL);
    }

    public void reset(membruSenat member, String siteURL)
            throws UnsupportedEncodingException, MessagingException {

        String token = RandomString.make(64);
        while(passwordResetTokenRepository.findByToken(token) != null) {
            token = RandomString.make(64);
        }
        PasswordResetToken newPasswordResetToken = new PasswordResetToken(member.getId(), token);
        passwordResetTokenRepository.save(newPasswordResetToken);
        sendResetEmail(member, newPasswordResetToken, siteURL);
    }

    private void sendVerificationEmail(membruSenat member, String siteURL)
            throws MessagingException, UnsupportedEncodingException {
        String toAddress = member.getEmail();
        String fromAddress = "AutoAccVerif@gmail.com";
        String senderName = "UNITBV ";
        String subject = "Please verify your registration";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you,<br>"
                + "UNITBV";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", member.getName());
        String verifyURL = siteURL + "/api/auth/verify?code=" + member.getVerificationCode();

        content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);

        mailSender.send(message);
    }

    private void sendResetEmail(membruSenat member, PasswordResetToken passwordResetToken, String siteURL)
            throws MessagingException, UnsupportedEncodingException {
        String toAddress = member.getEmail();
        String fromAddress = "AutoAccVerif@gmail.com";
        String senderName = "UNITBV ";
        String subject = "Please verify your password reset request";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to verify your password reset request:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">RESET</a></h3>"
                + "The token has a <b>24 hour availability</b> before expiring.<br><br>"
                + "Thank you,<br>"
                + "UNITBV";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", member.getName());
        String verifyURL = siteURL + "/api/auth/confirm_reset?code=" + passwordResetToken.getToken();

        content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);

        mailSender.send(message);
    }

    public boolean verify(String verificationCode) {
        membruSenat member = repo.findByVerificationCode(verificationCode);

        if (member == null || member.isVerifiedEmail()) {
            return false;
        } else {
            member.setVerificationCode(null);
            member.setVerifiedEmail(true);
            repo.save(member);
            return true;
        }
    }

    public boolean verifyResetCode(String resetCode) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(resetCode);

        if (passwordResetToken== null || new Date().after(passwordResetToken.getExpirationDate())) {
            return false;
        } else {
            String code = passwordResetToken.getToken();
            return (code.equals(resetCode));
        }
    }

}
