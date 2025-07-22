package com.project.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.employee.dto.CreateEmployeeRequest;
import com.project.employee.dto.EmployeeDto;
import com.project.employee.dto.UpdateEmployeeRequest;
import com.project.employee.model.Employee;
import com.project.employee.repository.EmployeeRepository;
import com.project.employee.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();

        // Generate tokens for testing
        adminToken = jwtUtil.generateAccessToken("admin", "ROLE_ADMIN");
        userToken = jwtUtil.generateAccessToken("user", "USER");
    }

    @Test
    void shouldCreateEmployee() throws Exception {
        // given
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPhone("1234567890");
        request.setPosition("Software Engineer");
        request.setSalary(50000.0);
        request.setStartWorkDate(LocalDate.now());

        // when
        ResultActions response = mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(request.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(request.getLastName())))
                .andExpect(jsonPath("$.email", is(request.getEmail())));
    }

    @Test
    void shouldCreateMultipleEmployees() throws Exception {
        // given
        List<CreateEmployeeRequest> requests = Arrays.asList(
                createEmployeeRequest("John", "Doe", "john@example.com"),
                createEmployeeRequest("Jane", "Smith", "jane@example.com")
        );

        // when
        ResultActions response = mockMvc.perform(post("/api/employees/bulk")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)));

        // then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].email", is("john@example.com")))
                .andExpect(jsonPath("$[1].email", is("jane@example.com")));
    }

    @Test
    void shouldUpdateEmployee() throws Exception {
        // given
        Employee employee = createAndSaveEmployee();
        UpdateEmployeeRequest request = new UpdateEmployeeRequest();
        request.setPosition("Senior Software Engineer");
        request.setSalary(60000.0);

        // when
        ResultActions response = mockMvc.perform(put("/api/employees/{id}", employee.getId())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position", is(request.getPosition())))
                .andExpect(jsonPath("$.salary", is(request.getSalary())));
    }

    @Test
    void shouldDeleteEmployee() throws Exception {
        // given
        Employee employee = createAndSaveEmployee();

        // when
        ResultActions response = mockMvc.perform(delete("/api/employees/{id}", employee.getId())
                .header("Authorization", "Bearer " + adminToken));

        // then
        response.andExpect(status().isOk());
        mockMvc.perform(get("/api/employees/{id}", employee.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteMultipleEmployees() throws Exception {
        // given
        Employee emp1 = createAndSaveEmployee();
        Employee emp2 = createAndSaveEmployee("jane@example.com");
        List<Long> ids = Arrays.asList(emp1.getId(), emp2.getId());

        // when
        ResultActions response = mockMvc.perform(delete("/api/employees/bulk")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ids)));

        // then
        response.andExpect(status().isOk());
        mockMvc.perform(get("/api/employees/{id}", emp1.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetEmployee() throws Exception {
        // given
        Employee employee = createAndSaveEmployee();

        // when
        ResultActions response = mockMvc.perform(get("/api/employees/{id}", employee.getId())
                .header("Authorization", "Bearer " + adminToken));

        // then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(employee.getId().intValue())))
                .andExpect(jsonPath("$.email", is(employee.getEmail())));
    }

    @Test
    void shouldGetAllEmployees() throws Exception {
        // given
        createAndSaveEmployee();
        createAndSaveEmployee("jane@example.com");

        // when
        ResultActions response = mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .param("page", "0")
                .param("size", "10"));

        // then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(2)));
    }

    @Test
    void shouldNotAllowUserToCreateEmployee() throws Exception {
        // given
        CreateEmployeeRequest request = createEmployeeRequest("John", "Doe", "john@example.com");

        // when
        ResultActions response = mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        response.andExpect(status().isForbidden());
    }

    private CreateEmployeeRequest createEmployeeRequest(String firstName, String lastName, String email) {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setEmail(email);
        request.setPhone("1234567890");
        request.setPosition("Software Engineer");
        request.setSalary(50000.0);
        request.setStartWorkDate(LocalDate.now());
        return request;
    }

    private Employee createAndSaveEmployee() {
        return createAndSaveEmployee("john@example.com");
    }

    private Employee createAndSaveEmployee(String email) {
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .phone("1234567890")
                .position("Software Engineer")
                .salary(50000.0)
                .startWorkDate(LocalDate.now())
                .build();
        return employeeRepository.save(employee);
    }
}
