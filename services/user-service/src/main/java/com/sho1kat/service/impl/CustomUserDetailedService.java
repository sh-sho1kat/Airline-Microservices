package com.sho1kat.service.impl;

import com.sho1kat.entity.User;
import com.sho1kat.exception.ApiException;
import com.sho1kat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CustomUserDetailedService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException(
                    "User not found with email: " + email
            );
        }

        ensureCanAuthenticate(user);

        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                authorities
        );
    }


    public Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles()
                .stream()
                .map(role ->
                        new SimpleGrantedAuthority(
                                "ROLE_" + role.name()
                        )
                )
                .toList();
    }

    public void ensureCanAuthenticate(User user) {
        switch (user.getStatus()) {
            case ACTIVE -> { }
            case PENDING_VERIFICATION -> throw ApiException.forbidden("Please verify your email before logging in");
            case PENDING_APPROVAL -> throw ApiException.forbidden("Your account is awaiting administrator approval");
            case REJECTED -> throw ApiException.forbidden("Your registration was not approved");
            case SUSPENDED -> throw ApiException.forbidden("Your account has been suspended");
            default -> throw ApiException.unauthorized("Invalid email or password");
        }
    }
}
