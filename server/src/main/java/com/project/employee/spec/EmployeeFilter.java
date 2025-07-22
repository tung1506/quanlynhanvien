package com.project.employee.spec;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeFilter {
    private String name;
    private String email;
    private String position;
    private String phone;
    private Double minSalary;
    private Double maxSalary;
    private LocalDate startWorkDateFrom;
    private LocalDate startWorkDateTo;
}
