package com.project.employee.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String username;
    private String role;
    private String refreshToken;
    private LocalDateTime createdAt;
}