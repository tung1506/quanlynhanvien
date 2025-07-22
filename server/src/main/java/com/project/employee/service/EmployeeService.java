package com.project.employee.service;

import com.project.employee.dto.CreateEmployeeRequest;
import com.project.employee.dto.EmployeeDto;
import com.project.employee.dto.UpdateEmployeeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.project.employee.spec.EmployeeFilter;

import java.util.List;

public interface EmployeeService {
    EmployeeDto createEmployee(CreateEmployeeRequest request);
    List<EmployeeDto> createEmployees(List<CreateEmployeeRequest> requests);
    EmployeeDto updateEmployee(Long id, UpdateEmployeeRequest request);
    void deleteEmployee(Long id);
    void deleteEmployees(List<Long> ids);
    Page<EmployeeDto> getAllEmployees(Pageable pageable);
    EmployeeDto getEmployeeById(Long id);
    Page<EmployeeDto> filterEmployees(EmployeeFilter filter, Pageable pageable);
}
