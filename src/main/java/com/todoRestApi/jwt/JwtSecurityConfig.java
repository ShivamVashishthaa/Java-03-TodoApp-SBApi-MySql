package com.todoRestApi.jwt;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class JwtSecurityConfig {

// creating user
	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails user = User
				.withUsername("Shivam")
				.password("{noop}himani")
				.authorities("read")
				.roles("USER")
				.build();

		return new InMemoryUserDetailsManager(user);
	}

//	encryption for password
	public BCryptPasswordEncoder passwordEncode() {
		return new BCryptPasswordEncoder();
	}
	
//	customize Filter chain for requests
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, HandlerMappingIntrospector introspector)
			throws Exception {

		// h2-console is a servlet
		// https://github.com/spring-projects/spring-security/issues/12310
		 httpSecurity.authorizeHttpRequests(auth ->
		 					auth.requestMatchers("/authenticate")
		 						.permitAll()
//                			    .requestMatchers(PathRequest.toH2Console()).permitAll() // h2-console is a servlet and NOT recommended for a production
		 						.requestMatchers(HttpMethod.OPTIONS, "/**")
		 						.permitAll()
		 						.anyRequest()
		 						.authenticated());
		 
		 httpSecurity.csrf(AbstractHttpConfigurer::disable);
		 
		 httpSecurity.sessionManagement(session -> 
					session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		 
		 httpSecurity.oauth2ResourceServer(oauth2 -> 
								oauth2.jwt(Customizer.withDefaults())); // enable jwt
				
		 httpSecurity.httpBasic(Customizer.withDefaults());
				
		 httpSecurity.headers(headers -> headers.frameOptions(
						frameOptionsConfig -> frameOptionsConfig.sameOrigin()));
				
		return httpSecurity.build();
	}

	
//	steps for JWT configuration
//step1 create key pair
	@Bean
	public KeyPair keyPair() {
		try {
			var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);   // initialize key size
			return keyPairGenerator.generateKeyPair();
		} catch (Exception e) {
			throw new IllegalStateException("Unable to generate an RSA Key Pair", e);
		}
	}

//step2 create RSA object using key pair by using library nimbus
	@Bean
	public RSAKey rsaKey() {
//		public RSAKey rsaKey(KeyPair keyPair) {

		KeyPair keyPair = keyPair();

		return new RSAKey
				.Builder((RSAPublicKey) keyPair.getPublic())
				.privateKey((RSAPrivateKey) keyPair.getPrivate())
				.keyID(UUID.randomUUID().toString()) 			// generate keyID
				.build();
	}

//	step3 create JWKSource
	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		JWKSet jwkSet = new JWKSet(rsaKey());
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}
//	step 4 jwtDecoder
	@Bean
	JwtDecoder jwtDecoder() throws JOSEException {
		return NimbusJwtDecoder.withPublicKey(rsaKey().toRSAPublicKey()).build();
	}
// step 5 jwtEncoder	
	@Bean
	JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
		return new NimbusJwtEncoder(jwkSource);
	}
	
//step 6 go to JwtAuthenticationController class
	@Bean
	public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
		var authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(userDetailsService);
		return new ProviderManager(authenticationProvider);
	}

}
