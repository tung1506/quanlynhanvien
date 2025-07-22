package com.project.employee.config;

import com.project.employee.model.User;
import com.project.employee.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder {

    @Bean
    public CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminUsername = "admin";
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setName("Administrator");
                admin.setPassword(passwordEncoder.encode("admin123")); // Change password for production
                admin.setRole("ROLE_ADMIN");
                userRepository.save(admin);
            }
        };
    }
}