package com.bezkoder.spring.security.postgresql.security.services;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.bezkoder.spring.security.postgresql.models.membruSenat;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
public class UserDetailsImpl implements UserDetails {
	private static final long serialVersionUID = 1L;

	private Long id;

	private String name;

	private String email;

	private boolean verifiedApplication;

	private boolean verifiedEmail;

	private boolean activated2FA;

	private boolean adminPriviledge;
	@JsonIgnore
	private String password;

	private Collection<? extends GrantedAuthority> authorities;

	public UserDetailsImpl(Long id, String name, String email, String password, boolean verifiedApplication, boolean verifiedEmail,
						   boolean activated2FA, boolean adminPriviledge, Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
		this.verifiedApplication = verifiedApplication;
		this.verifiedEmail = verifiedEmail;
		this.activated2FA = activated2FA;
		this.adminPriviledge = adminPriviledge;
		this.authorities = authorities;
	}

	public static UserDetailsImpl build(membruSenat member) {
		List<GrantedAuthority> authorities = member.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority(role.getName().name()))
				.collect(Collectors.toList());

		return new UserDetailsImpl(
				member.getId(),
				member.getName(),
				member.getEmail(),
				member.getPassword(),
				member.isVerifiedApplication(),
				member.isVerifiedEmail(),
				member.isActivated2FA(),
				member.isAdminPriviledge(),
				authorities);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return name;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return verifiedEmail;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserDetailsImpl user = (UserDetailsImpl) o;
		return Objects.equals(id, user.id);
	}
}
