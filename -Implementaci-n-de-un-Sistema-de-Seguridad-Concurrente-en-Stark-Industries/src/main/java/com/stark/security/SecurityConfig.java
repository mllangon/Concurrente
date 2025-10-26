package com.stark.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; style-src 'self' 'unsafe-inline'; connect-src 'self' wss:")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/login.html", "/ws/**", "/actuator/health/**", "/api/health/**", "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/actuator/metrics", "/actuator/metrics/**").permitAll()
                        .requestMatchers("/api/metrics/public").permitAll()
                        .requestMatchers("/api/sensors/public").permitAll()
                        .requestMatchers("/api/sensors/metrics").permitAll()
                        .requestMatchers("/api/messaging/status").permitAll()
                        .requestMatchers("/api/messaging/info").permitAll()
                        .requestMatchers("/api/messaging/test").permitAll()
                        .requestMatchers("/api/messaging/send-custom").permitAll()
                        .requestMatchers("/actuator/**").hasAnyRole("ADMIN", "SECURITY_ENGINEER")
                        .requestMatchers("/", "/index.html").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")))
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login.html?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll());

        http.headers(h -> h.frameOptions(f -> f.sameOrigin()));
        return http.build();
    }

    @Bean
    public UserDetailsService users(PasswordEncoder encoder) {
        UserDetails admin = User.withUsername("admin").password(encoder.encode("admin")).roles("ADMIN").build();
        UserDetails sec = User.withUsername("sec").password(encoder.encode("sec")).roles("SECURITY_ENGINEER").build();
        UserDetails op = User.withUsername("op").password(encoder.encode("op")).roles("OPERATOR").build();
        UserDetails view = User.withUsername("view").password(encoder.encode("view")).roles("VIEWER").build();
        return new InMemoryUserDetailsManager(admin, sec, op, view);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


