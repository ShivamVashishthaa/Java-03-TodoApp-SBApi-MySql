package com.todoRestApi.basic;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

//@Configuration
public class BasicAuthenticationSecurityConfiguration {
	//Filter chain
	//authenticate all requests
	//basic authentication
	//disabling csrf
	//stateless rest api
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//		enable authentication for every request
		http.authorizeHttpRequests(auth -> auth
										.requestMatchers(HttpMethod.OPTIONS,"/**")
										.permitAll()
										.anyRequest()
										.authenticated());
//		Enable basic athentication
		http.httpBasic(Customizer.withDefaults());
//		create session stateless
		http.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//		csrf disable
        http.csrf(csrf -> csrf.disable());
		return http.build();    //we can use method chaining as well
	}
}
