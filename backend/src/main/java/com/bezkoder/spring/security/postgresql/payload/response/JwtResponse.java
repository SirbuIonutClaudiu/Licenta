package com.bezkoder.spring.security.postgresql.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {
	private String token;
	private Long id;
	private List<String> roles;

	public JwtResponse(String accessToken, Long id, List<String> roles) {
		this.token = accessToken;
		this.id = id;
		this.roles = roles;
	}
}
