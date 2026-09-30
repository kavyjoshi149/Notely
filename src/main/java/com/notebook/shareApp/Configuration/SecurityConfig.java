package com.notebook.shareApp.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // fetch() calls to /api/** should get a 401 the JS can handle, not a 302 to the login page
        RequestMatcher apiRequest = request -> request.getRequestURI().startsWith("/api/");

        http
                .authorizeHttpRequests(auth -> auth
                        // pages and assets anyone can open
                        .requestMatchers("/", "/explore", "/login", "/register", "/error", "/favicon.ico",
                                "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/notes/*").permitAll()
                        // open JSON endpoints (dropdowns, stats, register, "who am I")
                        .requestMatchers("/api/public/**", "/api/me").permitAll()
                        // notes API: logged-in only for these three (must come before the {id} rule)
                        .requestMatchers("/api/notes/mine", "/api/notes/*/download").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/notes", "/api/notes/*").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // everything else (/upload, /library, POST /api/notes) needs a login
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), apiRequest)
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", false)   // false: go back to the page they were trying to open
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
