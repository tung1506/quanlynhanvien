package com.project.employee.service;

import com.project.employee.dto.*;
import com.project.employee.spec.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;
import java.util.List;

public interface UserService {
    UserDto createUser(CreateUserRequest request);
    List<UserDto> createUsers(List<CreateUserRequest> requests);
    UserDto updateUser(Long id, UpdateUserRequest request);
    List<UserDto> updateUsers(@Valid List<BulkUpdateUserRequest> users);
    void deleteUser(Long id);
    void deleteUsers(List<Long> ids);
    Page<UserDto> getAllUsers(Pageable pageable);
    Page<UserDto> filterUsers(UserFilter filter, Pageable pageable);
}
