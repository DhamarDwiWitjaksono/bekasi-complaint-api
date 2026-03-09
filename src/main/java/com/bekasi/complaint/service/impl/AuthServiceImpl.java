package com.bekasi.complaint.service.impl;

import com.bekasi.complaint.dto.request.LoginRequest;
import com.bekasi.complaint.dto.request.SignUpRequest;
import com.bekasi.complaint.dto.response.JwtResponse;
import com.bekasi.complaint.dto.response.UserResponse;
import com.bekasi.complaint.entity.Role;
import com.bekasi.complaint.entity.User;
import com.bekasi.complaint.enums.RoleName;
import com.bekasi.complaint.exception.BadRequestException;
import com.bekasi.complaint.exception.ResourceNotFoundException;
import com.bekasi.complaint.repository.RoleRepository;
import com.bekasi.complaint.repository.UserRepository;
import com.bekasi.complaint.security.JwtUtils;
import com.bekasi.complaint.security.UserDetailsImpl;
import com.bekasi.complaint.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public UserResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address is already in use: " + request.getEmail());
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Default user role not found. Please contact support."));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());
        return UserResponse.fromEntity(savedUser);
    }

    @Override
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        log.info("User logged in: {}", userDetails.getEmail());

        return JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(userDetails.getId())
                .name(userDetails.getName())
                .email(userDetails.getEmail())
                .roles(roles)
                .build();
    }
}
