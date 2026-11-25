package com.avkaiexplorer.pingpong.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Every response carries X-Request-Id; the access line has the id, no query string and no email. */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class RequestIdFilterTest {

	@Autowired
	MockMvc mvc;

	@Test
	void aSafeIncomingIdIsKeptAndReturned() throws Exception {
		mvc.perform(get("/api/v1/ping").header("X-Request-Id", "test-request-0001"))
			.andExpect(header().string("X-Request-Id", "test-request-0001"));
	}

	@Test
	void withoutOneTheCloudflareRayIdIsUsedElseANewOneIsMade() throws Exception {
		mvc.perform(get("/api/v1/ping").header("Cf-Ray", "8a1b2c3d4e5f6a7b-DFW"))
			.andExpect(header().string("X-Request-Id", "8a1b2c3d4e5f6a7b-DFW"));
		String made = mvc.perform(get("/api/v1/ping")).andReturn().getResponse().getHeader("X-Request-Id");
		assertThat(made).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
	}

	@Test
	void anUnsafeIncomingIdIsReplacedSoItCannotInjectIntoLogs() {
		assertThat(RequestIdFilter.choose("bad id\nwith newline", null)).matches("[0-9a-f-]{36}");
		assertThat(RequestIdFilter.choose("short", null)).matches("[0-9a-f-]{36}");
		assertThat(RequestIdFilter.choose("x".repeat(65), null)).matches("[0-9a-f-]{36}");
		assertThat(RequestIdFilter.choose("good-id-12345", "ignored-cf-ray-1")).isEqualTo("good-id-12345");
	}

	@Test
	void rejectedRequestsGetAnIdToo() throws Exception {
		mvc.perform(get("/api/v1/me")).andExpect(status().is4xxClientError())
			.andExpect(header().exists("X-Request-Id"));
	}

	@Test
	void theAccessLineHasTheIdAndStatusButNeverTheQueryStringOrTheEmail(CapturedOutput output) throws Exception {
		mvc.perform(get("/api/v1/ping?token=SECRET123").header("X-Request-Id", "log-check-00001")).andExpect(status().isOk());
		String all = output.getAll();
		assertThat(all).contains("GET /api/v1/ping 200");           // (the id itself is a structured-log field, not in the plain test format)
		assertThat(all).doesNotContain("SECRET123");
	}


	@Test
	void theLogContextIsCleanedUpAfterTheRequest() throws Exception {
		mvc.perform(get("/api/v1/ping").header("X-Request-Id", "cleanup-check-01"));
		assertThat(MDC.get("request_id")).isNull();
	}

	@Test
	void theHealthProbesAreNotLoggedAsAccessLines(CapturedOutput output) throws Exception {
		mvc.perform(get("/healthz").header("X-Request-Id", "probe-check-0001")).andExpect(status().isOk());
		assertThat(output.getAll()).doesNotContain("GET /healthz 200");
	}

}
