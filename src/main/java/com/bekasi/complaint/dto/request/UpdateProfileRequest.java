package com.bekasi.complaint.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;

    private String currentPassword;
}
