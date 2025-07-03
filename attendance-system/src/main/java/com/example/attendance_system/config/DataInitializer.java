package com.example.attendance_system.config;

import com.example.attendancesystem.model.User;
import com.example.attendancesystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("admin@example.com")
                        .roles(Set.of("ADMIN"))
                        .build();
                userRepository.save(admin);
            }
            if (userRepository.findByUsername("employee").isEmpty()) {
                User employee = User.builder()
                        .username("employee")
                        .password(passwordEncoder.encode("emp123"))
                        .email("employee@example.com")
                        .roles(Set.of("EMPLOYEE"))
                        .build();
                userRepository.save(employee);
            }
        };
    }
}
