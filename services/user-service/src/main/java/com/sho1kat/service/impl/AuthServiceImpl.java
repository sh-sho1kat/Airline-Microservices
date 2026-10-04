package com.sho1kat.service.impl;

import com.sho1kat.entity.User;
import com.sho1kat.payload.auth.response.AuthResponse;
import com.sho1kat.payload.dto.UserDto;
import com.sho1kat.repository.UserRepository;
import com.sho1kat.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public AuthResponse login(String username, String password) {
        return null;
    }

    /*
    1. Check if email already exists
    2. Encode password using BCrypt
    3. Save user in database
    4. Generate JWT token
    5. Return token and user information
    */
    @Override
    public AuthResponse signup(UserDto userDto) {
        User user = userRepository.findByEmail(userDto.getEmail());
        if (user != null) {
            return new AuthResponse(null, "Email already exists", "Error", null);
        }

        User newUser = User.builder()
                .email(userDto.getEmail())
                .passwordHash(passwordEncoder.encode(userDto.getPassword()))
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .roles(userDto.getUserRoles())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .lastLogin(Instant.now())
                .build();
        User savedUser = userRepository.save(newUser);

        return null;
    }
}
