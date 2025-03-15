package com.next.nest.controller;

import com.next.nest.dto.UserDTO;
import com.next.nest.dto.auth.AuthenticationRequest;
import com.next.nest.dto.auth.AuthenticationResponse;
import com.next.nest.dto.auth.RegisterRequest;
import com.next.nest.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Create a new user account with the provided details",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User registered successfully",
                content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input data"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Email or phone number already exists"
            )
        }
    )
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(
        summary = "Authenticate user",
        description = "Authenticate a user with email and password",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User authenticated successfully",
                content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid credentials"
            )
        }
    )
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh-token")
    @Operation(
        summary = "Refresh JWT token",
        description = "Get a new access token using refresh token",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Token refreshed successfully",
                content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Invalid refresh token"
            )
        }
    )
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        authService.refreshToken(request, response);
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get current user",
        description = "Get details of the currently authenticated user",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User details retrieved successfully",
                content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Not authenticated"
            )
        }
    )
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Logout user",
        description = "Logout the currently authenticated user",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User logged out successfully"
            )
        }
    )
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(request, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate-token")
    @Operation(
        summary = "Validate JWT token",
        description = "Check if a JWT token is valid",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Token validation result"
            )
        }
    )
    public ResponseEntity<Boolean> validateToken(
            @RequestBody String token
    ) {
        return ResponseEntity.ok(authService.validateToken(token));
    }
}