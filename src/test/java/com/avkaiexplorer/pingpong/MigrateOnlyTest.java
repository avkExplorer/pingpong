package com.avkaiexplorer.pingpong;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class MigrateOnlyTest {

	@Test
	void migrateOnlyStartsWithoutAWebServerAndExits() {
		// Uses the in-memory database from src/test/resources; a web server would bind a port and never return.
		assertThatCode(() -> PingpongApplication.main(new String[] { PingpongApplication.MIGRATE_ONLY })).doesNotThrowAnyException();
	}

}
