package com.sho1kat.service.impl;

import com.sho1kat.config.JwtProvider;
import com.sho1kat.entity.RefreshToken;
import com.sho1kat.entity.VerificationToken;
import com.sho1kat.entity.StaffProfile;
import com.sho1kat.entity.User;
import com.sho1kat.enums.TokenType;
import com.sho1kat.enums.UserRole;
import com.sho1kat.enums.UserStatus;
import com.sho1kat.exception.ApiException;
import com.sho1kat.mapper.StaffProfileMapper;
import com.sho1kat.mapper.UserMapper;
import com.sho1kat.payload.userservicedto.request.auth.*;
import com.sho1kat.payload.userservicedto.response.AuthResponse;
import com.sho1kat.payload.userservicedto.response.MessageResponse;
import com.sho1kat.repository.VerificationTokenRepository;
import com.sho1kat.repository.UserRepository;
import com.sho1kat.service.AuthService;
import com.sho1kat.service.MailService;
import com.sho1kat.service.TokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailedService customUserDetailedService;
    private final MailService mailService;
    private final TokenService tokenService;

    @Override
    @Transactional
    public AuthResponse signup(UserSignUpRequest request) {

        String email = normalizeEmail(request.getEmail());
        User existingUser = userRepository.findByEmail(email);

        if (existingUser != null) {
            return new AuthResponse(
                    null,
                    null,
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

        Authentication authentication = getNewAuthentication(savedUser);

        String jwt = jwtProvider.generateToken(
                authentication,
                savedUser.getId()
        );
        String refreshToken = tokenService.issueRefreshToken(savedUser);

        savedUser.setLastLogin(Instant.now());
        userRepository.save(savedUser);

        mailService.sendMail(email,"Welcome To Airline","Your Account has been created. Please Verify your email address.");
        return new AuthResponse(
                jwt,
                refreshToken,
                "Bearer",
                "Welcome: " + savedUser.getFirstName() + " " + savedUser.getLastName(),
                "User registered successfully",
                "Success",
                UserMapper.toResponse(savedUser)
        );
    }

    @Override
    public AuthResponse login(LogInRequest request) {

        String email = normalizeEmail(request.getEmail());

        Authentication authentication = authenticate(email, request.getPassword());

        User user = userRepository.findByEmail(email);
        user.setLastLogin(Instant.now());
        userRepository.save(user);

        String jwt =
                jwtProvider.generateToken(
                        authentication,
                        user.getId()
                );
        String refreshToken = tokenService.issueRefreshToken(user);

        return new AuthResponse(
                jwt,
                refreshToken,
                "Bearer",
                "Welcome: " + user.getFirstName() + " " + user.getLastName(),
                "User logged in successfully",
                "Success",
                UserMapper.toResponse(user)

        );
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken old = tokenService.consumeRefreshToken(request.getRefreshToken());
        User user = old.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw ApiException.unauthorized("Account is not active");
        }
        return new AuthResponse(
                jwtProvider.generateToken(getNewAuthentication(user), user.getId()),
                tokenService.issueRefreshToken(user),
                "Bearer",
                "Token refreshed successfully",
                "Refresh Token",
                "Success",
                UserMapper.toResponse(user)
        );
    }

    @Override
    public MessageResponse logout(LogoutRequest request) {
        tokenService.revokeRefreshToken(request.getRefreshToken());
        return new MessageResponse("Logged out");
    }

    @Override
    public MessageResponse sendVerification(EmailRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()));
        if(user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            String rawToken = tokenService.issueOneTimeToken(user, TokenType.EMAIL_VERIFICATION);
            mailService.sendVerificationEmail(user.getEmail(), user.getFirstName() + " " + user.getLastName(), rawToken);
        }

        return new MessageResponse("If the account exists and is unverified, a new verification email has been sent.");
    }

    @Override
    public MessageResponse registerStaff(StaffSignUpRequest request) {

        String email = normalizeEmail(request.getUser().getEmail());

        User user = userRepository.findByEmail(email);


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

        if (user.getStaffProfile() != null) {
            throw new IllegalStateException(
                    "Staff registration already exists for this user"
            );
        }

        StaffProfile staffProfile =
                StaffProfileMapper.toEntity(request);

        staffProfile.setApprovedAt(null);
        staffProfile.setApprovedBy(null);
        staffProfile.setRejectionReason(null);

        user.setStaffProfile(staffProfile);
        staffProfile.setUser(user);

        User savedUser = userRepository.save(user);

        return new MessageResponse("Registration received. Verify your email; an administrator will then "
                + "review your application.");
    }

    @Override
    public MessageResponse forgotPassword(EmailRequest request) {

        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email);

        if (user == null) {
            return new MessageResponse(
                    "If the email exists, a password reset link has been sent."
            );
        }

        verificationTokenRepository
                .deleteByUser_IdAndUsedAtIsNull(
                        user.getId()
                );

        String rawToken = tokenService.issueOneTimeToken(user, TokenType.PASSWORD_RESET);
        mailService.sendPasswordResetEmail(email,user.getFirstName()+" "+user.getLastName(), rawToken);

        return new MessageResponse(
                "If the email exists, a password reset link has been sent."
        );
    }

    @Override
    public MessageResponse resetPassword(ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(
                request.getConfirmPassword()
        )) {
            throw new IllegalArgumentException(
                    "New password and confirm password do not match"
            );
        }

        VerificationToken resetToken = tokenService.consumeOneTimeToken(
                request.getToken(),
                TokenType.PASSWORD_RESET
        );
        Optional<VerificationToken> optionalToken =
                verificationTokenRepository
                        .findByTokenHashAndUsedAtIsNull(
                                resetToken.getTokenHash()
                        );

        if (optionalToken.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid or expired reset token"
            );
        }

        if (resetToken.getExpiresAt()
                .isBefore(Instant.now())) {

            throw new IllegalArgumentException(
                    "Invalid or expired reset token"
            );
        }

        User user = resetToken.getUser();

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

        tokenService.revokeAllRefreshTokens(user);

        return new MessageResponse(
                "Password reset successfully"
        );
    }

    @Override
    public MessageResponse changePassword(String email, ChangePasswordRequest request) {

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

    private Authentication authenticate(String email, String password) {

        UserDetails userDetails = customUserDetailedService.loadUserByUsername(email);

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

    private Authentication getNewAuthentication(User user) {
        Collection<? extends GrantedAuthority> authorities = CustomUserDetailedService.getAuthorities(user);
        return new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                authorities
        );
    }

    private String normalizeEmail(String email) {

        return email
                .trim()
                .toLowerCase();
    }

}