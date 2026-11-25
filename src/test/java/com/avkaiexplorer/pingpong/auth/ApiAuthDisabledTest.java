package com.avkaiexplorer.pingpong.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** With no issuer configured (the ci stack, or before `avk auth client` has run) only the public paths work; the rest is 401. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiAuthDisabledTest {

	@Autowired
	MockMvc mvc;

	@Test
	void publicPathsStillWork() throws Exception {
		mvc.perform(get("/api/v1/ping")).andExpect(status().isOk());
		mvc.perform(get("/readyz")).andExpect(status().isOk());
	}

	@Test
	void protectedPathsAnswer401() throws Exception {
		for (String path : new String[] { "/admin", "/api/v1/me" }) {
			mvc.perform(get(path)).andExpect(status().isUnauthorized());
		}
	}

}
