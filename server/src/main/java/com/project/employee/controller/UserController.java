package com.project.employee.controller;

import com.project.employee.dto.BulkUpdateUserRequest;
import com.project.employee.dto.CreateUserRequest;
import com.project.employee.dto.UpdateUserRequest;
import com.project.employee.dto.UserDto;
import com.project.employee.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.project.employee.spec.UserFilter;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> createUsers(@Valid @RequestBody List<CreateUserRequest> requests) {
        return ResponseEntity.ok(userService.createUsers(requests));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id,
                                            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PutMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> updateUsers(@Valid @RequestBody List<BulkUpdateUserRequest> users) {
        return ResponseEntity.ok(userService.updateUsers(users));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUsers(@RequestBody List<Long> ids) {
        userService.deleteUsers(ids);
        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('ADMIN')")
    public PagedResponse<UserDto> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return new PagedResponse<>(userService.getAllUsers(PageRequest.of(page, size)));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<UserDto>> filterUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UserFilter filter = new UserFilter();
        filter.setUsername(username);
        filter.setName(name);
        filter.setRole(role);

        Pageable pageable = PageRequest.of(page, size);
        Page<UserDto> userPage = userService.filterUsers(filter, pageable);
        return ResponseEntity.ok(new PagedResponse<>(userPage));
    }
}