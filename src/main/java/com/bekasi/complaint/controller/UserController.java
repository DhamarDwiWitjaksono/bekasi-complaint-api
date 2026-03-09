package com.bekasi.complaint.controller;

import com.bekasi.complaint.dto.request.UpdateProfileRequest;
import com.bekasi.complaint.dto.response.ApiResponse;
import com.bekasi.complaint.dto.response.UserResponse;
import com.bekasi.complaint.security.UserDetailsImpl;
import com.bekasi.complaint.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users/me
     * Returns the authenticated user's profile (id, name, email, roles).
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        UserResponse profile = userService.getProfile(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    /**
     * PUT /api/users/me
     * Updates the authenticated user's name and/or password.
     * - name        : optional, updated if provided
     * - newPassword : optional, requires currentPassword to be provided as well
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserResponse updated = userService.updateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }
}