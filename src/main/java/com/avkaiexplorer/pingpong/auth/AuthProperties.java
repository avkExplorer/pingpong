package com.avkaiexplorer.pingpong.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Login settings, all from the environment (written by `avk auth client`): the public issuer the browser sees, the
 * internal issuer the app calls for token, key and user-info requests, and the client's id and secret.
 * With no client id the app runs without login: public pages work, everything else answers 401.
 */
@ConfigurationProperties(prefix = "avk.auth")
public record AuthProperties(String issuer, String internalIssuer, String clientId, String clientSecret) {

	public boolean enabled() {
		return clientId != null && !clientId.isBlank();
	}

	/** Where containers reach Keycloak; falls back to the public issuer when no internal address is set. */
	public String backchannel() {
		return internalIssuer == null || internalIssuer.isBlank() ? issuer : internalIssuer;
	}

}
