package com.bekasi.complaint.service;

import com.bekasi.complaint.dto.request.LoginRequest;
import com.bekasi.complaint.dto.request.SignUpRequest;
import com.bekasi.complaint.dto.response.JwtResponse;
import com.bekasi.complaint.dto.response.UserResponse;

public interface AuthService {
    UserResponse signUp(SignUpRequest request);
    JwtResponse login(LoginRequest request);
}
