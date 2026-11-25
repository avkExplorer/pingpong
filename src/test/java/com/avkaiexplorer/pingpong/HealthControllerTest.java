package com.avkaiexplorer.pingpong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.simple.JdbcClient;

class HealthControllerTest {

	@Test
	void readinessReports503WhenTheDatabaseIsDown() {
		JdbcClient jdbc = mock(JdbcClient.class);
		when(jdbc.sql("select 1")).thenThrow(new DataAccessResourceFailureException("down"));

		var response = new HealthController(jdbc).ready();

		assertThat(response.getStatusCode().value()).isEqualTo(503);
		assertThat(response.getBody()).containsEntry("status", "database unavailable");
	}

	@Test
	void livenessNeverTouchesTheDatabase() {
		JdbcClient jdbc = mock(JdbcClient.class);

		assertThat(new HealthController(jdbc).live()).containsEntry("status", "up");
	}

}
