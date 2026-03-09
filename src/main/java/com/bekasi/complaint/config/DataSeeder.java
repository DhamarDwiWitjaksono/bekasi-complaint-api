package com.bekasi.complaint.config;

import com.bekasi.complaint.entity.Role;
import com.bekasi.complaint.entity.User;
import com.bekasi.complaint.enums.RoleName;
import com.bekasi.complaint.repository.RoleRepository;
import com.bekasi.complaint.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seeder.admin.name}")
    private String adminName;

    @Value("${app.seeder.admin.email}")
    private String adminEmail;

    @Value("${app.seeder.admin.password}")
    private String adminPassword;

    @Value("${app.seeder.officer.name}")
    private String officerName;

    @Value("${app.seeder.officer.email}")
    private String officerEmail;

    @Value("${app.seeder.officer.password}")
    private String officerPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedAdminAccount();
        seedOfficerAccount();
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Seeded role: {}", roleName);
            }
        }
    }

    private void seedAdminAccount() {
        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User admin = User.builder()
                    .name(adminName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of(adminRole))
                    .build();

            userRepository.save(admin);
            log.info("Seeded admin account: {}", adminEmail);
        }
    }

    private void seedOfficerAccount() {
        if (!userRepository.existsByEmail(officerEmail)) {
            Role officerRole = roleRepository.findByName(RoleName.ROLE_OFFICER)
                    .orElseThrow(() -> new RuntimeException("Officer role not found"));

            User officer = User.builder()
                    .name(officerName)
                    .email(officerEmail)
                    .password(passwordEncoder.encode(officerPassword))
                    .roles(Set.of(officerRole))
                    .build();

            userRepository.save(officer);
            log.info("Seeded officer account: {}", officerEmail);
        }
    }
}
