package com.priya.devflow.service;

import com.priya.devflow.dto.Dtos.CreateUserRequest;
import com.priya.devflow.dto.Dtos.UserDto;
import com.priya.devflow.entity.AppUser;
import com.priya.devflow.exception.ConflictException;
import com.priya.devflow.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<UserDto> list() {
        return users.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public UserDto create(CreateUserRequest request) {
        if (users.existsByUsername(request.username())) {
            throw new ConflictException("Username already exists");
        }
        AppUser saved = users.save(new AppUser(request.username(), encoder.encode(request.password()), request.role()));
        return toDto(saved);
    }

    private UserDto toDto(AppUser u) {
        return new UserDto(u.getId(), u.getUsername(), u.getRole(), u.getCreatedAt());
    }
}
