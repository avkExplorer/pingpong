package com.avkaiexplorer.pingpong.auth;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** Turns Keycloak's realm roles into Spring roles. Only the two platform roles count; anything else is ignored. */
final class Roles {

	private static final Set<String> KNOWN = Set.of("user", "admin");

	private Roles() {
	}

	static Set<GrantedAuthority> from(Map<String, Object> claims) {
		Set<GrantedAuthority> out = new LinkedHashSet<>();
		Object access = claims.get("realm_access");
		if (access instanceof Map<?, ?> map && map.get("roles") instanceof List<?> roles) {
			for (Object role : roles) {
				if (role instanceof String name && KNOWN.contains(name)) {
					out.add(new SimpleGrantedAuthority("ROLE_" + name.toUpperCase()));
				}
			}
		}
		return out;
	}

}
