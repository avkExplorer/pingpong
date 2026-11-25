package com.avkaiexplorer.pingpong.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class RolesTest {

	@Test
	void mapsTheTwoPlatformRoles() {
		var claims = Map.<String, Object>of("realm_access", Map.of("roles", List.of("user", "admin")));
		assertThat(Roles.from(claims)).extracting("authority").containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
	}

	@Test
	void ignoresKeycloaksOwnRolesAndAnythingUnknown() {
		var claims = Map.<String, Object>of("realm_access",
				Map.of("roles", List.of("offline_access", "default-roles-avk", "superuser", "user")));
		assertThat(Roles.from(claims)).extracting("authority").containsExactly("ROLE_USER");
	}

	@Test
	void missingOrMalformedClaimsGiveNoRoles() {
		assertThat(Roles.from(Map.of())).isEmpty();
		assertThat(Roles.from(Map.of("realm_access", "admin"))).isEmpty();
		assertThat(Roles.from(Map.of("realm_access", Map.of("roles", "admin")))).isEmpty();
	}

	@Test
	void backchannelFallsBackToThePublicIssuer() {
		assertThat(new AuthProperties("https://a/realms/avk", "", "c", "s").backchannel()).isEqualTo("https://a/realms/avk");
		assertThat(new AuthProperties("https://a/realms/avk", "http://k:8080/realms/avk", "c", "s").backchannel())
			.isEqualTo("http://k:8080/realms/avk");
		assertThat(new AuthProperties("", "", "", "").enabled()).isFalse();
	}

}
