package com.sho1kat.service.impl;

import com.sho1kat.config.JwtProvider;
import com.sho1kat.entity.PasswordResetToken;
import com.sho1kat.entity.StaffProfile;
import com.sho1kat.entity.User;
import com.sho1kat.enums.UserRole;
import com.sho1kat.mapper.StaffProfileMapper;
import com.sho1kat.mapper.UserMapper;
import com.sho1kat.payload.userservicedto.request.auth.*;
import com.sho1kat.payload.userservicedto.response.AuthResponse;
import com.sho1kat.payload.userservicedto.response.MessageResponse;
import com.sho1kat.payload.userservicedto.response.UserResponse;
import com.sho1kat.repository.PasswordResetTokenRepository;
import com.sho1kat.repository.UserRepository;
import com.sho1kat.service.AuthService;
import com.sho1kat.service.CustomUserDetailsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public AuthResponse signup(UserSignUpRequest request) {

        String email = normalizeEmail(request.getEmail());
        User existingUser = userRepository.findByEmail(email);

        if (existingUser != null) {
            return new AuthResponse(
                    null,
                    "User with email " + email + " already exists",
                    "Error",
                    "Failure",
                    null
            );
        }

        User newUser = UserMapper.toEntity(
                request,
                passwordEncoder.encode(request.getPassword())
        );

        newUser.setEmail(email);
        newUser.setEmailVerified(false);
        newUser.setRoles(
                new HashSet<>(Set.of(UserRole.USER))
        );

        User savedUser = userRepository.save(newUser);

        Collection<? extends GrantedAuthority> authorities =
                getAuthorities(savedUser);

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

        savedUser.setLastLogin(Instant.now());
        userRepository.save(savedUser);

        AuthResponse authResponse = new AuthResponse();

        authResponse.setJwt(jwt);
        authResponse.setTitle(
                "Welcome: " +
                        savedUser.getFirstName() +
                        " " +
                        savedUser.getLastName()
        );
        authResponse.setMessage(
                "User registered successfully"
        );
        authResponse.setStatus("Success");

        /*
         * If your AuthResponse currently expects UserDto,
         * use UserMapper.toDto(savedUser) here.
         */
        authResponse.setUser(
                UserMapper.toResponse(savedUser)
        );

        return authResponse;
    }

    @Override
    public AuthResponse login(
            LogInRequest request
    ) {

        String email = normalizeEmail(request.getEmail());

        Authentication authentication =
                authenticate(
                        email,
                        request.getPassword()
                );

        User user =
                userRepository.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        String jwt =
                jwtProvider.generateToken(
                        authentication,
                        user.getId()
                );

        AuthResponse authResponse = new AuthResponse();
        authResponse.setJwt(jwt);
        authResponse.setTitle(
                "Welcome: " +
                        user.getFirstName() +
                        " " +
                        user.getLastName()
        );
        authResponse.setMessage(
                "User logged in successfully"
        );
        authResponse.setStatus("Success");

        authResponse.setUser(
                UserMapper.toResponse(user)
        );

        return authResponse;
    }

    @Override
    public UserResponse registerStaff(StaffSignUpRequest request) {

        String email = normalizeEmail(request.getUser().getEmail());

        User user = userRepository.findByEmail(email);

        /*
         * If this email does not exist,
         * create a normal USER account first.
         */
        if (user == null) {

            user = UserMapper.toEntity(
                    request.getUser(),
                    passwordEncoder.encode(
                            request.getUser().getPassword()
                    )
            );

            user.setEmail(email);
            user.setEmailVerified(false);

            user.setRoles(
                    new HashSet<>(
                            Set.of(UserRole.USER)
                    )
            );
        }

        /*
         * The same user cannot submit another
         * staff registration while one already exists.
         */
        if (user.getStaffProfile() != null) {
            throw new IllegalStateException(
                    "Staff registration already exists for this user"
            );
        }

        StaffProfile staffProfile =
                StaffProfileMapper.toEntity(request);

        /*
         * Staff is not approved yet.
         */
        staffProfile.setApprovedAt(null);
        staffProfile.setApprovedBy(null);
        staffProfile.setRejectionReason(null);

        user.setStaffProfile(staffProfile);
        staffProfile.setUser(user);

        User savedUser = userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }

    @Override
    public MessageResponse forgotPassword(
            ForgotPasswordRequest request
    ) {

        String email =
                normalizeEmail(request.getEmail());

        User user =
                userRepository.findByEmail(email);

        /*
         * Do not reveal whether this email exists.
         */
        if (user == null) {
            return new MessageResponse(
                    "If the email exists, a password reset link has been sent."
            );
        }

        /*
         * Remove old unused reset tokens.
         */
        passwordResetTokenRepository
                .deleteByUser_IdAndUsedAtIsNull(
                        user.getId()
                );

        /*
         * Raw token is sent to the user.
         * Only the hash is stored in the database.
         */
        String rawToken =
                UUID.randomUUID().toString();

        String tokenHash =
                hashToken(rawToken);

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .user(user)
                        .tokenHash(tokenHash)
                        .expiresAt(
                                Instant.now()
                                        .plusSeconds(15 * 60)
                        )
                        .build();

        passwordResetTokenRepository.save(
                resetToken
        );

        /*
         * DEVELOPMENT ONLY.
         *
         * In production, send this token
         * through email instead.
         */
        System.out.println(
                "Password reset token for " +
                        email +
                        ": " +
                        rawToken
        );

        return new MessageResponse(
                "If the email exists, a password reset link has been sent."
        );
    }

    @Override
    public MessageResponse resetPassword(
            ResetPasswordRequest request
    ) {

        if (!request.getNewPassword().equals(
                request.getConfirmPassword()
        )) {
            throw new IllegalArgumentException(
                    "New password and confirm password do not match"
            );
        }

        String tokenHash =
                hashToken(request.getToken());

        Optional<PasswordResetToken> optionalToken =
                passwordResetTokenRepository
                        .findByTokenHashAndUsedAtIsNull(
                                tokenHash
                        );

        if (optionalToken.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid or expired reset token"
            );
        }

        PasswordResetToken resetToken =
                optionalToken.get();

        if (resetToken.getExpiresAt()
                .isBefore(Instant.now())) {

            throw new IllegalArgumentException(
                    "Invalid or expired reset token"
            );
        }

        User user =
                resetToken.getUser();

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        /*
         * Token cannot be used again.
         */
        resetToken.setUsedAt(
                Instant.now()
        );

        passwordResetTokenRepository.save(
                resetToken
        );

        return new MessageResponse(
                "Password reset successfully"
        );
    }

    @Override
    public MessageResponse changePassword(
            String email,
            ChangePasswordRequest request
    ) {

        if (!request.getNewPassword().equals(
                request.getConfirmPassword()
        )) {
            throw new IllegalArgumentException(
                    "New password and confirm password do not match"
            );
        }

        String normalizedEmail =
                normalizeEmail(email);

        User user =
                userRepository.findByEmail(
                        normalizedEmail
                );

        if (user == null) {
            throw new IllegalArgumentException(
                    "User not found"
            );
        }

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                    "Current password is incorrect"
            );
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                    "New password must be different from current password"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        return new MessageResponse(
                "Password changed successfully"
        );
    }

    private Authentication authenticate(
            String email,
            String password
    ) {

        UserDetails userDetails =
                customUserDetailsService
                        .loadUserByUsername(email);

        if (!passwordEncoder.matches(
                password,
                userDetails.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(),
                null,
                userDetails.getAuthorities()
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(
            User user
    ) {

        return user.getRoles()
                .stream()
                .map(role ->
                        new SimpleGrantedAuthority(
                                "ROLE_" + role.name()
                        )
                )
                .toList();
    }

    private String normalizeEmail(
            String email
    ) {

        return email
                .trim()
                .toLowerCase();
    }

    private String hashToken(
            String token
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            token.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder hex =
                    new StringBuilder();

            for (byte b : hash) {
                hex.append(
                        String.format(
                                "%02x",
                                b
                        )
                );
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    e
            );
        }
    }
}