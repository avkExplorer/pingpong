package com.avkaiexplorer.pingpong;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class PingpongApplication {

	/** Flag used by the platform's migration step: apply Flyway migrations, then exit without serving traffic. */
	static final String MIGRATE_ONLY = "--migrate-only";

	public static void main(String[] args) {
		if (Arrays.asList(args).contains(MIGRATE_ONLY)) {
			// Flyway runs while the context starts; a non-web context never opens the HTTP port.
			new SpringApplicationBuilder(PingpongApplication.class).web(WebApplicationType.NONE).run(args).close();
			return;
		}
		SpringApplication.run(PingpongApplication.class, args);
	}

}
