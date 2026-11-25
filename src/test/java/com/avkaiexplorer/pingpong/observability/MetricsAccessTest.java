package com.avkaiexplorer.pingpong.observability;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Metrics are readable only on the management port; the public port never serves them. */
@SpringBootTest(properties = "management.server.port=8081")
@AutoConfigureMockMvc
class MetricsAccessTest {

	@Autowired
	MockMvc mvc;

	@Test
	void theMainPortNeverServesMetricsToAnonymousVisitors() throws Exception {
		mvc.perform(get("/actuator/prometheus").with(request -> {
			request.setLocalPort(8080);
			return request;
		})).andExpect(status().is4xxClientError());
	}

	@Test
	void theManagementPortAllowsOnlyThePrometheusEndpointWithoutLogin() throws Exception {
		mvc.perform(get("/actuator/health").with(request -> {
			request.setLocalPort(8081);
			return request;
		})).andExpect(status().is4xxClientError());             // any other path on that port still needs a login
	}

}
