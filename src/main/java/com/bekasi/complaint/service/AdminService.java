package com.bekasi.complaint.service;

import com.bekasi.complaint.dto.request.CreateAccountRequest;
import com.bekasi.complaint.dto.response.UserResponse;

public interface AdminService {
    UserResponse createAccount(CreateAccountRequest request);
}
