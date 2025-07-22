package com.project.employee.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String name;
    private String username;
    private String role;
}
