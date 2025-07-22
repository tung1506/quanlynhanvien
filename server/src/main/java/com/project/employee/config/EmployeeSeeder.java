package com.project.employee.config;

import com.project.employee.model.Employee;
import com.project.employee.repository.EmployeeRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class EmployeeSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final Faker faker = new Faker();

    @Override
    public void run(String... args) {
        if (employeeRepository.count() < 10) { // Only seed if database is empty
            List<Employee> employees = new ArrayList<>();
            String[] positions = {
                "Software Engineer", "Senior Developer", "Project Manager",
                "Business Analyst", "QA Engineer", "DevOps Engineer",
                "Product Manager", "UI/UX Designer", "Data Scientist",
                "Technical Lead"
            };

            Set<String> usedEmails = new HashSet<>();

            for (int i = 0; i < 1000; i++) {
                String firstName = faker.name().firstName();
                String lastName = faker.name().lastName();
                // Create unique email by combining name and unique number
                String email = generateUniqueEmail(firstName, lastName, i, usedEmails);

                Employee employee = Employee.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(email)
                        .phone(faker.numerify("##########")) // 10 digit phone number
                        .position(positions[faker.random().nextInt(positions.length)])
                        .salary(40000.0 + faker.random().nextDouble() * 60000.0)
                        .startWorkDate(LocalDate.ofInstant(
                            faker.date().past(730, TimeUnit.DAYS).toInstant(),
                            ZoneId.systemDefault()
                        ))
                        .build();
                employees.add(employee);

                // Insert in batches of 100
                if ((i + 1) % 100 == 0 && !employees.isEmpty()) {
                    employeeRepository.saveAll(employees);
                    employees.clear();
                }
            }

            // Save any remaining employees
            if (!employees.isEmpty()) {
                employeeRepository.saveAll(employees);
            }
        }
    }

    private String generateUniqueEmail(String firstName, String lastName, int counter, Set<String> usedEmails) {
        String baseEmail = firstName.toLowerCase().replaceAll("[^a-z]", "") + "." +
                          lastName.toLowerCase().replaceAll("[^a-z]", "") +
                          counter + "@company.com";

        while (usedEmails.contains(baseEmail)) {
            baseEmail = firstName.toLowerCase().replaceAll("[^a-z]", "") + "." +
                       lastName.toLowerCase().replaceAll("[^a-z]", "") +
                       counter + faker.random().nextInt(1000) + "@company.com";
        }

        usedEmails.add(baseEmail);
        return baseEmail;
    }
}
