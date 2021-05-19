package com.bezkoder.spring.security.postgresql.controllers;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.bezkoder.spring.security.postgresql.models.PasswordResetToken;
import com.bezkoder.spring.security.postgresql.models.membruSenat;
import com.bezkoder.spring.security.postgresql.payload.request.*;
import com.bezkoder.spring.security.postgresql.repository.PasswordResetTokenRepository;
import com.bezkoder.spring.security.postgresql.repository.membruSenatRepository;
import com.bezkoder.spring.security.postgresql.security.services.MembruSenatService;
import com.bezkoder.spring.security.postgresql.security.services.UserServices;
import com.bezkoder.spring.security.postgresql.sms.Service;
import com.bezkoder.spring.security.postgresql.sms.SmsRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.bezkoder.spring.security.postgresql.models.ERole;
import com.bezkoder.spring.security.postgresql.models.Role;
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
	PasswordResetTokenRepository passwordResetTokenRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	private UserServices service;

	@Autowired
	private Service smsService;

	private Long memberId;
	private String jwt;
	private List<String> roles;


	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);
		
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();		
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());

		membruSenat member = membruSenatRepo.findByEmail(loginRequest.getEmail())
				.orElseThrow(() -> new RuntimeException("No such member !"));
		member.setLoginLocation(loginRequest.getLoginLocation());
		membruSenatRepo.save(member);

		if(userDetails.isActivated2FA()) {
			SmsRequest smsRequest = new SmsRequest(userDetails.getId());
			smsService.sendSms(smsRequest);
			this.memberId = userDetails.getId();
			this.jwt = jwt;
			this.roles = roles;
			return ResponseEntity.ok(new MessageResponse("Passed the login stage!"));
		}

		return ResponseEntity.ok(new JwtResponse( jwt,
				                                  userDetails.getId(),
				                                  roles));
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
			redirectView.setUrl("http://localhost:4200/email_successfull/successfull");
			return redirectView;
		} else {
			RedirectView redirectView = new RedirectView();
			redirectView.setUrl("http://localhost:4200/email_successfull/unsuccessfull");
			return redirectView;
		}
	}

	@PostMapping("/two_factor")
	public ResponseEntity<?> sendSms(@Valid @RequestBody SmsRequestBody smsRequest) {
		Long memberId = this.getMemberId();
		List<String> roles = this.getRoles();
		String jwt = this.getJwt();

		membruSenat member = membruSenatRepo.findById(memberId)
				.orElseThrow(() -> new RuntimeException("No such member !"));

		if(member.isActivated2FA()) {
			String db_code = member.getSmsCode();
			String req_code = smsRequest.getCode();
			if(!db_code.equals(req_code)) {
				return ResponseEntity
						.badRequest()
						.body(new MessageResponse("Wrong sms code ! Ty to get another code."));
			}
			member.setSmsCode(null);
			membruSenatRepo.save(member);
			this.setMemberId(null);
			this.setRoles(null);
			this.setJwt(null);
		}

		this.setMemberId(null);
		this.setRoles(null);
		this.setJwt(null);
		return ResponseEntity.ok(new JwtResponse(jwt,
				                                 member.getId(),
				                                 roles));
	}

	@PostMapping("/send_reset")
	public ResponseEntity<?> sendResetEmail(@Valid @RequestBody ResetRequest resetRequest, HttpServletRequest request)
			throws MessagingException, UnsupportedEncodingException {
		membruSenat member = membruSenatRepo.findByEmail(resetRequest.getEmail())
				.orElseThrow(() -> new RuntimeException("Email address not found!"));
		PasswordResetToken passToken = passwordResetTokenRepository.findByMemberId(member.getId());
		if(passToken != null) {
			if(LocalDate.now().isAfter(passToken.getExpirationDate())) {
				passwordResetTokenRepository.deleteById(passToken.getId());
			}
			else {
				return ResponseEntity
						.badRequest()
						.body(new MessageResponse("Code already sent. Check your email!"));
			}
		}
		try {
			service.reset(member, getSiteURL(request));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		this.setMemberId(member.getId());
		return ResponseEntity.ok(new MessageResponse("Reset email sent successfully. Check your email!"));
	}

	@GetMapping("/confirm_reset")
	public RedirectView ConfirmReset(@Param("code") String code) {
		if (service.verifyResetCode(code)) {
			RedirectView redirectView = new RedirectView();
			redirectView.setUrl("http://localhost:4200/change_password/" + code);
			return redirectView;
		} else {
			RedirectView redirectView = new RedirectView();
			redirectView.setUrl("http://localhost:4200/change_password/" + code);
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

	@PostMapping("/resend_sms/{email}")
	public ResponseEntity<?> resendSMS(@PathVariable("email") String email) {
		membruSenat member = membruSenatRepo.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User does not exist!"));
		SmsRequest smsRequest = new SmsRequest(member.getId());
		smsService.sendSms(smsRequest);
		return ResponseEntity.ok(new MessageResponse("Sms sent successfully! Check your phone."));
	}
}
