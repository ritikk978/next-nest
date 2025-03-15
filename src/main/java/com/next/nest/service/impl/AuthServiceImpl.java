package com.next.nest.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.next.nest.dto.UserDTO;
import com.next.nest.dto.auth.AuthenticationRequest;
import com.next.nest.dto.auth.AuthenticationResponse;
import com.next.nest.dto.auth.RegisterRequest;
import com.next.nest.entity.User;
import com.next.nest.exception.BadRequestException;
import com.next.nest.exception.EmailAlreadyExistsException;
import com.next.nest.exception.PhoneAlreadyExistsException;
import com.next.nest.exception.UnauthorizedException;
import com.next.nest.repository.UserRepository;
import com.next.nest.security.JwtService;
import com.next.nest.service.AuthService;
import com.next.nest.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // Validate if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        // Validate if phone exists
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new PhoneAlreadyExistsException("Phone number already exists: " + request.getPhoneNumber());
        }

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .emailVerified(false)
                .phoneVerified(false)
                .enabled(true)
                .build();

        var savedUser = userRepository.save(user);
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        }

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            var user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                var authResponse = buildAuthResponse(user, accessToken, refreshToken);
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    @Override
    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new UnauthorizedException("User not authenticated");
        }

        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userService.findByEmail(email);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // In a stateless JWT system, we can't invalidate tokens on the server
        // Client should remove tokens from their storage
        // For additional security, maintain a token blacklist or use short-lived tokens
        SecurityContextHolder.clearContext();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new BadRequestException("User not found"));
            
            return jwtService.isTokenValid(token, user);
        } catch (Exception e) {
            return false;
        }
    }

    private AuthenticationResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .profileImageUrl(user.getProfileImageUrl())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .build();
    }
}