package com.avkaiexplorer.pingpong;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

	private final JdbcClient jdbc;

	public HealthController(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	/** Liveness: the process is up. Never touches dependencies. */
	@GetMapping("/healthz")
	public Map<String, String> live() {
		return Map.of("status", "up");
	}

	/** Readiness: dependencies are reachable (the database answers, when the app has one). */
	@GetMapping("/readyz")
	public ResponseEntity<Map<String, String>> ready() {
		try {
			jdbc.sql("select 1").query(Integer.class).single();
			return ResponseEntity.ok(Map.of("status", "ready"));
		}
		catch (RuntimeException ex) {
			return ResponseEntity.status(503).body(Map.of("status", "database unavailable"));
		}
	}

}
