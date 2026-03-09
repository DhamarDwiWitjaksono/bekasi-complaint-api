package com.bekasi.complaint.service;

import com.bekasi.complaint.dto.request.UpdateProfileRequest;
import com.bekasi.complaint.dto.response.UserResponse;

public interface UserService {
    UserResponse getProfile(Long userId);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    
}
