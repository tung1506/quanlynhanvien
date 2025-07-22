package com.project.employee.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BulkUpdateUserRequest {
    private Long id;
    private String name;
    private String username;

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Password must be at least 8 characters long and contain at least one letter and one number")
    private String password;

    private String role;
}
