package com.bekasi.complaint.controller;

import com.bekasi.complaint.dto.request.LoginRequest;
import com.bekasi.complaint.dto.request.SignUpRequest;
import com.bekasi.complaint.dto.response.ApiResponse;
import com.bekasi.complaint.dto.response.JwtResponse;
import com.bekasi.complaint.dto.response.UserResponse;
import com.bekasi.complaint.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/signup
     * Register a new user account. Default role is USER.
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        UserResponse user = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", user));
    }

    /**
     * POST /api/auth/login
     * Authenticate user and return JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse jwtResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", jwtResponse));
    }

    /**
     * POST /api/auth/logout
     * Logout (client-side token invalidation; server stateless).
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success(
                "Logged out successfully. Please discard your token on the client side."));
    }
}
