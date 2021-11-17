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
package com.bezkoder.spring.security.postgresql.controllers;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import com.bezkoder.spring.security.postgresql.awsecrets.TwilioSecrets;
import com.bezkoder.spring.security.postgresql.models.*;
import com.bezkoder.spring.security.postgresql.payload.request.*;
import com.bezkoder.spring.security.postgresql.payload.response.LoginResponse;
import com.bezkoder.spring.security.postgresql.repository.PasswordResetTokenRepository;
import com.bezkoder.spring.security.postgresql.repository.membruSenatRepository;
import com.bezkoder.spring.security.postgresql.security.services.MembruSenatService;
import com.bezkoder.spring.security.postgresql.security.services.TemporaryAccessTokenService;
import com.bezkoder.spring.security.postgresql.security.services.UserServices;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.bezkoder.spring.security.postgresql.payload.response.JwtResponse;
import com.bezkoder.spring.security.postgresql.payload.response.MessageResponse;
import com.bezkoder.spring.security.postgresql.repository.RoleRepository;
import com.bezkoder.spring.security.postgresql.security.jwt.JwtUtils;
import com.bezkoder.spring.security.postgresql.security.services.UserDetailsImpl;
import org.springframework.web.servlet.view.RedirectView;

@CrossOrigin(origins = "*", maxAge = 3600)
@Getter
@Setter
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	membruSenatRepository membruSenatRepo;

	@Autowired
	MembruSenatService membruSenatService;

	@Autowired
	PasswordResetTokenRepository passwordResetTokenRepository;

	@Autowired
	TemporaryAccessTokenService temporaryAccessTokenService;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	private UserServices service;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		membruSenat member = membruSenatRepo.findByEmail(loginRequest.getEmail())
				.orElseThrow(() -> new RuntimeException("No such member !"));
		if(!member.isVerifiedApplication()) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Your register application has not been reviewed by a moderator yet!"));
		}
		if(member.isDisabled()) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Your account has been temporarily disabled by an Administrator or Moderator!"));
		}
		member.setLoginLocation(loginRequest.getLoginLocation());
		membruSenatRepo.save(member);

		if(userDetails.isActivated2FA()) {
			TemporaryAccessToken newTemporaryAccessToken = temporaryAccessTokenService.saveNewAccessToken(member.getId(), loginRequest.getPassword());
			SendPhoneVerificationRequest sendPhoneVerificationRequest = new SendPhoneVerificationRequest(member.getEmail(), newTemporaryAccessToken.getToken());
			if(!this.sendPhoneVerification(sendPhoneVerificationRequest)) {
				return ResponseEntity
						.badRequest()
						.body(new MessageResponse("Verification code could not be sent!"));
			}
			return ResponseEntity.ok(new LoginResponse(newTemporaryAccessToken.getToken(), member.getPhoneNumber()));
		}

		String jwt = jwtUtils.generateJwtToken(authentication);
		List<String> roles = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());
		return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), roles));
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest, HttpServletRequest request)
			throws MessagingException, UnsupportedEncodingException {

		if (membruSenatRepo.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user's account
		membruSenat member = new membruSenat(signUpRequest.getName(),
							 signUpRequest.getInstitutionalCode(),
							 signUpRequest.getAddress(),
							 signUpRequest.getEmail(),
							 encoder.encode(signUpRequest.getPassword()),
				             signUpRequest.getApplicationDate(),
				             signUpRequest.isVerifiedApplication(),
				             signUpRequest.isVerifiedEmail(),
				             signUpRequest.isActivated2FA());

		Set<Role> roles = new HashSet<>();

		Role userRole = roleRepository.findByName(ERole.ROLE_USER)
				.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
		roles.add(userRole);

		member.setRoles(roles);

		service.register(member, getSiteURL(request));

		membruSenatRepo.save(member);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}

	private String getSiteURL(HttpServletRequest request) {
		String siteURL = request.getRequestURL().toString();
		return siteURL.replace(request.getServletPath(), "");
	}

	@GetMapping("/verify")
	public RedirectView verifyUser(@Param("code") String code) {
		if (service.verify(code)) {
			RedirectView redirectView = new RedirectView();
			redirectView.setUrl("http://elasticbeanstalk-us-east-2-602307895584.s3-website.us-east-2.amazonaws.com/email_successfull/successfull");
			return redirectView;
		} else {
			RedirectView redirectView = new RedirectView();
			redirectView.setUrl("http://elasticbeanstalk-us-east-2-602307895584.s3-website.us-east-2.amazonaws.com/unsuccessfull");
			return redirectView;
		}
	}

	@PostMapping("/sendPhoneVerification")
	public boolean PostSendPhoneVerification(@Valid @RequestBody SendPhoneVerificationRequest sendPhoneVerificationRequest) {
		if(!membruSenatRepo.existsByEmail(sendPhoneVerificationRequest.getMemberEmail())) {
			return false;
		}
		membruSenat member = membruSenatService.findMemberByEmail(sendPhoneVerificationRequest.getMemberEmail());
		TemporaryAccessToken temporaryAccessToken = temporaryAccessTokenService.findByMemberId(member.getId());
		if(!temporaryAccessToken.getToken().equals(sendPhoneVerificationRequest.getAccessToken())) {
			return false;
		}
		if(new Date().after(temporaryAccessToken.getExpirationDate())) {
			return false;
		}
		Twilio.init(new TwilioSecrets("TwilioAccountSID").getSecret(),
				new TwilioSecrets("TwilioAuthToken").getSecret());
		if(member.getVerificationSID() != null) {
			com.twilio.rest.verify.v2.Service.deleter(member.getVerificationSID()).delete();
		}
		com.twilio.rest.verify.v2.Service service = com.twilio.rest.verify.v2.Service.creator("UnitbVoting").create();
		Verification verification = Verification.creator(
				service.getSid(),
				member.getPhoneNumber(),
				"sms")
				.create();
		member.setVerificationSID(service.getSid());
		member.setPhoneNumber2BVerified(member.getPhoneNumber());
		membruSenatRepo.save(member);
		return true;
	}

	public boolean sendPhoneVerification(SendPhoneVerificationRequest sendPhoneVerificationRequest) {
		if(!membruSenatRepo.existsByEmail(sendPhoneVerificationRequest.getMemberEmail())) {
			return false;
		}
		membruSenat member = membruSenatService.findMemberByEmail(sendPhoneVerificationRequest.getMemberEmail());
		TemporaryAccessToken temporaryAccessToken = temporaryAccessTokenService.findByMemberId(member.getId());
		if(!temporaryAccessToken.getToken().equals(sendPhoneVerificationRequest.getAccessToken())) {
			return false;
		}
		if(new Date().after(temporaryAccessToken.getExpirationDate())) {
			return false;
		}
		Twilio.init(new TwilioSecrets("TwilioAccountSID").getSecret(),
				new TwilioSecrets("TwilioAuthToken").getSecret());
		if(member.getVerificationSID() != null) {
			com.twilio.rest.verify.v2.Service.deleter(member.getVerificationSID()).delete();
		}
		com.twilio.rest.verify.v2.Service service = com.twilio.rest.verify.v2.Service.creator("UnitbVoting").create();
		Verification verification = Verification.creator(
				service.getSid(),
				member.getPhoneNumber(),
				"sms")
				.create();
		member.setVerificationSID(service.getSid());
		member.setPhoneNumber2BVerified(member.getPhoneNumber());
		membruSenatRepo.save(member);
		return true;
	}

	@PostMapping("/confirm_code")
	public ResponseEntity<?> confirmPhone(@Valid @RequestBody AuthCodeConfirmationRequest authCodeConfirmationRequest) {
		membruSenat member = membruSenatRepo.findByEmail(authCodeConfirmationRequest.getMemberEmail())
				.orElseThrow(() -> new RuntimeException("User does not exist!"));
		TemporaryAccessToken temporaryAccessToken = temporaryAccessTokenService.findByMemberId(member.getId());
		if(!authCodeConfirmationRequest.getAccessToken().equals(temporaryAccessToken.getToken())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Temporary Access Token provided is not valid !"));
		}
		if(new Date().after(temporaryAccessToken.getExpirationDate())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Temporary Access Token expired.Try to log in again !"));
		}
		if(member.getVerificationSID() == null) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Try sending another verification request !"));
		}
		Twilio.init(new TwilioSecrets("TwilioAccountSID").getSecret(),
				new TwilioSecrets("TwilioAuthToken").getSecret());
		try {
			VerificationCheck verificationCheck = VerificationCheck.creator(
					member.getVerificationSID(),
					authCodeConfirmationRequest.getConfirmationCode())
					.setTo(member.getPhoneNumber2BVerified()).create();
			if(verificationCheck.getStatus().toLowerCase(Locale.ROOT).compareToIgnoreCase("approved") == 0) {
				member.setVerificationSID(null);
				member.setPhoneNumber(member.getPhoneNumber2BVerified());
				member.setPhoneNumber2BVerified(null);
				membruSenatRepo.save(member);
			}
			else {
				return ResponseEntity
						.badRequest()
						.body(new MessageResponse("Wrong code.Try again !"));
			}
		} catch(Exception e) {
			com.twilio.rest.verify.v2.Service.deleter(member.getVerificationSID()).delete();
			member.setVerificationSID(null);
			member.setPhoneNumber2BVerified(null);
			membruSenatRepo.save(member);
			return ResponseEntity
					.badRequest()
					.body("Code request expired.Try sending another one !");
		}

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(member.getEmail(), temporaryAccessToken.getLoginRequestPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());
		temporaryAccessTokenService.deleteToken(temporaryAccessToken);
		return ResponseEntity.ok(new JwtResponse(jwt, member.getId(), roles));
	}

	@PostMapping("/send_reset")
	public ResponseEntity<?> sendResetEmail(@Valid @RequestBody ResetRequest resetRequest, HttpServletRequest request) throws ParseException {
		membruSenat member = membruSenatRepo.findByEmail(resetRequest.getEmail())
				.orElseThrow(() -> new RuntimeException("Email address not found!"));
		PasswordResetToken passToken = passwordResetTokenRepository.findByMemberId(member.getId());
		if(passToken != null) {
			Calendar sendTime = Calendar.getInstance();
			sendTime.setTime(passToken.getExpirationDate());
			sendTime.add(Calendar.HOUR, -23);
			if(new Date().after(sendTime.getTime())) {
				passwordResetTokenRepository.deleteById(passToken.getId());
			}
			else {
				return ResponseEntity
						.badRequest()
						.body(new MessageResponse("Code can be requested once per hour. Check your email!"));
			}
		}
		try {
			service.reset(member, getSiteURL(request));
		} catch (UnsupportedEncodingException | MessagingException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok(new MessageResponse("Reset email sent successfully. Check your email!"));
	}

	@GetMapping("/confirm_reset")
	public RedirectView ConfirmReset(@Param("code") String code) {
		if (service.verifyResetCode(code)) {
			RedirectView redirectView = new RedirectView();
			redirectView.setUrl("http://elasticbeanstalk-us-east-2-602307895584.s3-website.us-east-2.amazonaws.com/change_password/" + code);
			return redirectView;
		} else {
			RedirectView redirectView = new RedirectView();
			redirectView.setUrl("http://elasticbeanstalk-us-east-2-602307895584.s3-website.us-east-2.amazonaws.com/change_password/" + code);
			return redirectView;
		}
	}

	@PostMapping("/reset_password")
	public ResponseEntity<?> ResetPassword(@Valid @RequestBody ChangeRequest changeRequest) {
		if (service.verifyResetCode(changeRequest.getCode())) {
			PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(changeRequest.getCode());
			membruSenat member = membruSenatRepo.findById(passwordResetToken.getMemberId())
					.orElseThrow(() -> new RuntimeException("User to access does not exist!"));
			passwordResetTokenRepository.deleteById(passwordResetToken.getId());
			member.setPassword(encoder.encode(changeRequest.getPassword()));
			membruSenatRepo.save(member);
			return ResponseEntity.ok(new MessageResponse("Password reset successfully!"));
		} else {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Code expired or invalid!"));
		}
	}
}
