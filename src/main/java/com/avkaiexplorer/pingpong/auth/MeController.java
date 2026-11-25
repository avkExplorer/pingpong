package com.avkaiexplorer.pingpong.auth;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

	/** Who the token says the caller is: the login subject and the platform roles. */
	@GetMapping("/api/v1/me")
	public Map<String, Object> me(Authentication authentication) {
		List<String> roles = authentication.getAuthorities().stream().map(a -> a.getAuthority()).sorted().toList();
		return Map.of("subject", authentication.getName(), "roles", roles);
	}

	/** An admin-only area, enforced by the security configuration (a normal user gets 403 on the direct URL). */
	@GetMapping("/admin")
	public Map<String, String> admin() {
		return Map.of("area", "admin");
	}

}
