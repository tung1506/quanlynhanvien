package com.project.employee.service.impl;

import com.project.employee.dto.CreateEmployeeRequest;
import com.project.employee.dto.EmployeeDto;
import com.project.employee.dto.UpdateEmployeeRequest;
import com.project.employee.exception.ApiException;
import com.project.employee.mapper.EmployeeMapper;
import com.project.employee.model.Employee;
import com.project.employee.repository.EmployeeRepository;
import com.project.employee.service.EmployeeService;
import com.project.employee.spec.EmployeeFilter;
import com.project.employee.spec.EmployeeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public EmployeeDto createEmployee(CreateEmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email already exists");
        }

        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .position(request.getPosition())
                .salary(request.getSalary())
                .startWorkDate(request.getStartWorkDate())
                .build();

        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Override
    public List<EmployeeDto> createEmployees(List<CreateEmployeeRequest> requests) {
        List<Employee> employees = requests.stream()
                .map(request -> {
                    if (employeeRepository.existsByEmail(request.getEmail())) {
                        throw new ApiException("Email already exists: " + request.getEmail());
                    }

                    return Employee.builder()
                            .firstName(request.getFirstName())
                            .lastName(request.getLastName())
                            .email(request.getEmail())
                            .phone(request.getPhone())
                            .position(request.getPosition())
                            .salary(request.getSalary())
                            .startWorkDate(request.getStartWorkDate())
                            .build();
                })
                .toList();

        return employeeRepository.saveAll(employees).stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    @Override
    public EmployeeDto updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ApiException("Employee not found"));

        if (request.getEmail() != null && !request.getEmail().equals(employee.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email already exists");
        }

        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            employee.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }
        if (request.getPosition() != null) {
            employee.setPosition(request.getPosition());
        }
        if (request.getSalary() != null) {
            employee.setSalary(request.getSalary());
        }
        if (request.getStartWorkDate() != null) {
            employee.setStartWorkDate(request.getStartWorkDate());
        }

        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Override
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ApiException("Employee not found");
        }
        employeeRepository.deleteById(id);
    }

    @Override
    public void deleteEmployees(List<Long> ids) {
        employeeRepository.deleteAllById(ids);
    }

    @Override
    public Page<EmployeeDto> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(employeeMapper::toDto);
    }

    @Override
    public EmployeeDto getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .map(employeeMapper::toDto)
                .orElseThrow(() -> new ApiException("Employee not found"));
    }

    @Override
    public Page<EmployeeDto> filterEmployees(EmployeeFilter filter, Pageable pageable) {
        Specification<Employee> spec = EmployeeSpecification.withFilter(filter);
        return employeeRepository.findAll(spec, pageable).map(employeeMapper::toDto);
    }
}
