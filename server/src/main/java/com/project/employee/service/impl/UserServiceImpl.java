package com.project.employee.service.impl;

import com.project.employee.dto.*;
import com.project.employee.mapper.UserMapper;
import com.project.employee.model.User;
import com.project.employee.repository.UserRepository;
import com.project.employee.service.UserService;
import com.project.employee.spec.UserFilter;
import com.project.employee.spec.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto createUser(CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public List<UserDto> createUsers(List<CreateUserRequest> requests) {
        List<User> users = requests.stream()
                .map(request -> {
                    User user = new User();
                    user.setName(request.getName());
                    user.setUsername(request.getUsername());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setRole(request.getRole());
                    return user;
                })
                .collect(Collectors.toList());

        List<User> savedUsers = userRepository.saveAll(users);
        return savedUsers.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public List<UserDto> updateUsers(List<BulkUpdateUserRequest> users) {
        List<User> updatedUsers = users.stream()
                .map(user -> {
                    User existingUser = userRepository.findById(user.getId())
                            .orElseThrow(() -> new RuntimeException("User not found with id: " + user.getId()));

                    if (user.getName() != null) {
                        existingUser.setName(user.getName());
                    }
                    if (user.getUsername() != null) {
                        existingUser.setUsername(user.getUsername());
                    }
                    if (user.getPassword() != null) {
                        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
                    }
                    if (user.getRole() != null) {
                        existingUser.setRole(user.getRole());
                    }
                    return userRepository.save(existingUser);
                })
                .collect(Collectors.toList());

        return updatedUsers.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public void deleteUsers(List<Long> ids) {
        userRepository.deleteAllById(ids);
    }

    @Override
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Override
    public Page<UserDto> filterUsers(UserFilter filter, Pageable pageable) {
        Specification<User> spec = UserSpecification.withFilter(filter);
        return userRepository.findAll(spec, pageable).map(userMapper::toDto);
    }
}
