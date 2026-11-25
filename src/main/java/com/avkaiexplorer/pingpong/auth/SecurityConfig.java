package com.avkaiexplorer.pingpong.auth;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Bearer-token API: every request that is not public must carry an access token issued by the shared Keycloak realm. No cookies,
 * no sessions, no CSRF (there is nothing a browser could be tricked into sending). The token's issuer must be exactly the public
 * issuer; its signing keys are fetched from the internal address. Roles come from the realm roles in the token; /admin is
 * enforced here on the server. With no OIDC_ISSUER the API answers 401 for everything that is not public.
 */
@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class SecurityConfig {

	@Bean
	@ConditionalOnExpression("!'${avk.auth.issuer:}'.isBlank()")
	JwtDecoder jwtDecoder(AuthProperties auth) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(auth.backchannel() + "/protocol/openid-connect/certs").build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(auth.issuer()));
		return decoder;
	}

	/** Web only: `--migrate-only` starts the app without a web server, and then there is no HttpSecurity to configure. */
	@Bean
	@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
	SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectProvider<JwtDecoder> decoders,
			@Value("${management.server.port:-1}") int managementPort) throws Exception {
		http.authorizeHttpRequests(requests -> requests
			// Metrics for Prometheus: only on the separate management port (never published, never routed by Traefik), only this endpoint.
			.requestMatchers(request -> managementPort > 0 && request.getLocalPort() == managementPort
					&& "/actuator/prometheus".equals(request.getRequestURI())).permitAll()
			.requestMatchers("/healthz", "/readyz", "/api/v1/ping", "/error").permitAll()
			.requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
			.anyRequest().authenticated());
		http.csrf(csrf -> csrf.disable());
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		JwtDecoder decoder = decoders.getIfAvailable();
		if (decoder == null) {
			http.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
			return http.build();
		}
		http.oauth2ResourceServer(resource -> resource.jwt(jwt -> jwt.decoder(decoder)
			.jwtAuthenticationConverter(token -> new JwtAuthenticationToken(token, Roles.from(token.getClaims()), token.getSubject()))));
		return http.build();
	}

}
