package com.project.employee.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateEmployeeRequest {
    private String firstName;
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;
    private String phone;
    private String position;
    private Double salary;
    private LocalDate startWorkDate;
}
