package com.bekasi.complaint.service.impl;

import com.bekasi.complaint.dto.request.CreateAccountRequest;
import com.bekasi.complaint.dto.response.UserResponse;
import com.bekasi.complaint.entity.Role;
import com.bekasi.complaint.entity.User;
import com.bekasi.complaint.enums.RoleName;
import com.bekasi.complaint.exception.BadRequestException;
import com.bekasi.complaint.exception.ResourceNotFoundException;
import com.bekasi.complaint.repository.RoleRepository;
import com.bekasi.complaint.repository.UserRepository;
import com.bekasi.complaint.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createAccount(CreateAccountRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address is already in use: " + request.getEmail());
        }

        RoleName roleName = request.getRole();

        // Admin can only create ADMIN or OFFICER accounts via this endpoint
        if (roleName == RoleName.ROLE_USER) {
            throw new BadRequestException("Use the public sign-up endpoint to create user accounts.");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(role))
                .build();

        User savedUser = userRepository.save(user);
        log.info("Admin created new account: {} with role: {}", savedUser.getEmail(), roleName);
        return UserResponse.fromEntity(savedUser);
    }
}
