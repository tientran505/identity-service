package com.example.identity_service.configuration;

import java.util.HashSet;

import com.example.identity_service.entity.Role;
import com.example.identity_service.repository.RoleRepository;
import lombok.experimental.NonFinal;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.identity_service.entity.User;
import com.example.identity_service.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                roleRepository.save(Role.builder()
                                .name(com.example.identity_service.enums.Role.USER.name())
                                .description("User role")
                        .build());

                Role adminRole = Role.builder()
                        .name(com.example.identity_service.enums.Role.ADMIN.name())
                        .description("Admin role")
                        .build();

                HashSet<Role> roles = new HashSet<>();
                roleRepository.save(adminRole);

                roles.add(adminRole);

                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .roles(roles)
                        .build();

                userRepository.save(user);
                log.warn("Admin user has been created with default password: admin");
            }
            ;
        };
    }
    ;
}
