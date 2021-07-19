package com.bezkoder.spring.security.postgresql.controllers;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
	MembruSenatService membruSenatService;

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

	private String jwt;
	private List<String> roles;
	private Long loginID = 0L;
	private final String username = "AC315b0b103eacf332065bb30dca612446";
	private final String password = "25153cfe99974d8a4a802d26abffec49";

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);
		
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();		
		List<String> roles = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());

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
			this.loginID = userDetails.getId();
			if(!this.sendPhoneVerification()) {
				return ResponseEntity
						.badRequest()
						.body(new MessageResponse("Verification code could not be sent!"));
			}
			this.jwt = jwt;
			this.roles = roles;
			return ResponseEntity.ok(new MessageResponse("Passed the login stage!"));
		}

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
			redirectView.setUrl("http://localhost:4200/email_successfull/successfull");
			return redirectView;
		} else {
			RedirectView redirectView = new RedirectView();
			redirectView.setUrl("http://localhost:4200/email_successfull/unsuccessfull");
			return redirectView;
		}
	}

	@PostMapping("/sendPhoneVerification")
	public boolean sendPhoneVerification() {
		if(!membruSenatRepo.existsById(this.loginID)) {
			return false;
		}
		membruSenat member = membruSenatService.findMemberById(this.loginID);
		Twilio.init(this.username, this.password);
		if(member.getVerificationSID() != null) {
			com.twilio.rest.verify.v2.Service.deleter(member.getVerificationSID()).delete();
		}
		com.twilio.rest.verify.v2.Service service = com.twilio.rest.verify.v2.Service.creator("UNITBV Voting").create();
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

	@PostMapping("/confirm_code/{code}")
	public ResponseEntity<?> confirmPhone(@PathVariable("code") String code) {
		membruSenat member = membruSenatRepo.findById(this.loginID)
				.orElseThrow(() -> new RuntimeException("User does not exist!"));
		if(member.getVerificationSID() == null) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Try sending another verification request !"));
		}
		Twilio.init(this.username, this.password);
		try {
			VerificationCheck verificationCheck = VerificationCheck.creator(
					member.getVerificationSID(),
					code)
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
		Long loginID_aux = this.loginID;
		this.loginID = 0L;
		return ResponseEntity.ok(new JwtResponse(jwt, loginID_aux, this.roles));
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
		} catch (UnsupportedEncodingException | MessagingException e) {
			e.printStackTrace();
		}
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
}
