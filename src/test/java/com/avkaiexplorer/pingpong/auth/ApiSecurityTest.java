package com.avkaiexplorer.pingpong.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/** Bearer-token rules with the resource server configured (no Keycloak needed: tokens are supplied by the test). */
@SpringBootTest(properties = { "avk.auth.issuer=https://auth.example.test/realms/avk",
		"avk.auth.internal-issuer=http://keycloak:8080/realms/avk", "avk.auth.client-id=svc" })
@AutoConfigureMockMvc
class ApiSecurityTest {

	@Autowired
	MockMvc mvc;

	@Test
	void publicPathsNeedNoToken() throws Exception {
		mvc.perform(get("/api/v1/ping")).andExpect(status().isOk());
		mvc.perform(get("/healthz")).andExpect(status().isOk());
	}

	@Test
	void everythingElseAnswers401WithoutAToken() throws Exception {
		for (String path : new String[] { "/api/v1/me", "/admin", "/anything-else" }) {
			mvc.perform(get(path)).andExpect(status().isUnauthorized());
		}
	}

	@Test
	void aTokenShowsWhoIsCallingAndWhichRoles() throws Exception {
		mvc.perform(get("/api/v1/me").with(jwt().jwt(j -> j.subject("abc-123")).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
			.andExpect(status().isOk()).andExpect(jsonPath("$.subject").value("abc-123")).andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
	}

	@Test
	void aNormalUserGets403OnAdminAndAnAdminGetsIn() throws Exception {
		mvc.perform(get("/admin").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isForbidden());
		mvc.perform(get("/admin").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))).andExpect(status().isOk());
	}

}
