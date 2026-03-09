package com.bekasi.complaint.controller;

import com.bekasi.complaint.dto.request.CreateAccountRequest;
import com.bekasi.complaint.dto.response.ApiResponse;
import com.bekasi.complaint.dto.response.UserResponse;
import com.bekasi.complaint.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * POST /api/admin/accounts
     * Create a new ADMIN or OFFICER account.
     * Only accessible by ADMIN role.
     */
    @PostMapping("/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        UserResponse user = adminService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", user));
    }
}
