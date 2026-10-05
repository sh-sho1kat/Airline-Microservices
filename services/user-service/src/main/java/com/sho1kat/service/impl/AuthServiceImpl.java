package com.sho1kat.service.impl;

import com.sho1kat.config.JwtProvider;
import com.sho1kat.entity.User;
import com.sho1kat.enums.UserRole;
import com.sho1kat.mapper.UserMapper;
import com.sho1kat.payload.auth.response.AuthResponse;
import com.sho1kat.payload.dto.UserDto;
import com.sho1kat.repository.UserRepository;
import com.sho1kat.service.AuthService;
import com.sho1kat.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public AuthResponse signUp(UserDto userDto) {

        User user = userRepository.findByEmail(userDto.getEmail());

        if (user != null) {
            return new AuthResponse(
                    null,
                    "User with email " + userDto.getEmail() + " already exists",
                    "Error",
                    "Failure",
                    null
            );
        }

        User newUser = User.builder()
                .email(userDto.getEmail())
                .emailVerified(false)
                .passwordHash(passwordEncoder.encode(userDto.getPassword()))
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .roles(Set.of(UserRole.ADMIN))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .lastLogin(Instant.now())
                .build();

        User savedUser = userRepository.save(newUser);

        Collection<? extends GrantedAuthority> authorities =
                savedUser.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.toString()))
                        .toList();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        savedUser.getEmail(),
                        null,
                        authorities
                );

        String jwt = jwtProvider.generateToken(
                authentication,
                savedUser.getId()
        );

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setTitle(
                "Welcome: " +
                        savedUser.getFirstName() + " " +
                        savedUser.getLastName()
        );
        authResponse.setMessage("User registered successfully");
        authResponse.setStatus("Success");
        authResponse.setUser(UserMapper.toDto(savedUser));

        return authResponse;
    }

    @Override
    public AuthResponse logIn(String email, String password) {

        Authentication authentication = authenticate(email, password);

        User user = userRepository.findByEmail(email);

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        String jwt = jwtProvider.generateToken(
                authentication,
                user.getId()
        );

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setTitle(
                "Welcome: " +
                        user.getFirstName() + " " +
                        user.getLastName()
        );
        authResponse.setMessage("User logged in successfully");
        authResponse.setStatus("Success");
        authResponse.setUser(UserMapper.toDto(user));

        return authResponse;
    }

    private Authentication authenticate(String email, String password) {

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(email);

        if (!passwordEncoder.matches(
                password,
                userDetails.getPassword()
        )) {
            throw new RuntimeException("Invalid password");
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(),
                null,
                userDetails.getAuthorities()
        );
    }
}