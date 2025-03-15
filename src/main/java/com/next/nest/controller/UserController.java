package com.next.nest.controller;

import com.next.nest.dto.UserDTO;
import com.next.nest.entity.enums.UserRole;
import com.next.nest.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(
        summary = "Get user by ID",
        description = "Get user details by user ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User details retrieved successfully",
                content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found"
            )
        }
    )
    public ResponseEntity<UserDTO> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id
    ) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all users",
        description = "Get paginated list of all users (Admin only)",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Users retrieved successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Access denied"
            )
        }
    )
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userService.findAll(page, size));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get users by role",
        description = "Get paginated list of users by role (Admin only)",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Users retrieved successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Access denied"
            )
        }
    )
    public ResponseEntity<Page<UserDTO>> getUsersByRole(
            @Parameter(description = "User role") @PathVariable UserRole role,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userService.findAllByRole(role, page, size));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Search users",
        description = "Search users by name or email with optional role filter (Admin only)",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Search results retrieved successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Access denied"
            )
        }
    )
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @Parameter(description = "Search term") @RequestParam String query,
            @Parameter(description = "User role (optional)") @RequestParam(required = false) UserRole role,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userService.searchUsers(query, role, page, size));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update user",
        description = "Update user details",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User updated successfully",
                content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input data"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Email or phone number already exists"
            )
        }
    )
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO
    ) {
        return ResponseEntity.ok(userService.update(id, userDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete user",
        description = "Delete user by ID (Admin only)",
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "User deleted successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Access denied"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found"
            )
        }
    )
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id
    ) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    @Operation(
        summary = "Change password",
        description = "Change user password",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Password changed successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid password"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Current password is incorrect"
            )
        }
    )
    public ResponseEntity<Void> changePassword(
            @RequestBody Map<String, String> passwordData
    ) {
        userService.updatePassword(
                Long.parseLong(passwordData.get("userId")),
                passwordData.get("oldPassword"),
                passwordData.get("newPassword")
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(
        summary = "Request password reset",
        description = "Send password reset link to user's email",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Password reset email sent successfully"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found"
            )
        }
    )
    public ResponseEntity<Void> resetPassword(
            @RequestBody Map<String, String> resetData
    ) {
        userService.resetPassword(resetData.get("email"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-reset-password")
    @Operation(
        summary = "Confirm password reset",
        description = "Reset password using token received via email",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Password reset successful"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid or expired token"
            )
        }
    )
    public ResponseEntity<Void> confirmResetPassword(
            @RequestBody Map<String, String> resetData
    ) {
        userService.confirmResetPassword(
                resetData.get("token"),
                resetData.get("newPassword")
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify-email")
    @Operation(
        summary = "Verify email",
        description = "Verify user email using token",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Email verified successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid or expired token"
            )
        }
    )
    public ResponseEntity<Void> verifyEmail(
            @Parameter(description = "Verification token") @RequestParam String token
    ) {
        userService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification")
    @Operation(
        summary = "Resend verification email",
        description = "Resend email verification link",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Verification email sent successfully"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found"
            )
        }
    )
    public ResponseEntity<Void> resendVerificationEmail(
            @RequestBody Map<String, String> emailData
    ) {
        userService.resendVerificationEmail(emailData.get("email"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get user statistics",
        description = "Get user registration statistics (Admin only)",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Statistics retrieved successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Access denied"
            )
        }
    )
    public ResponseEntity<Map<String, Object>> getUserStats() {
        long totalTenants = userService.countByRole(UserRole.TENANT);
        long totalLandlords = userService.countByRole(UserRole.LANDLORD);
        long totalBrokers = userService.countByRole(UserRole.BROKER);
        long todayRegistrations = userService.countTodayRegistrations();
        
        Map<String, Object> stats = Map.of(
            "totalTenants", totalTenants,
            "totalLandlords", totalLandlords,
            "totalBrokers", totalBrokers,
            "todayRegistrations", todayRegistrations
        );
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get recent users",
        description = "Get list of recently registered users (Admin only)",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Recent users retrieved successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Access denied"
            )
        }
    )
    public ResponseEntity<List<UserDTO>> getRecentUsers(
            @Parameter(description = "Number of users to return") @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(userService.findRecentUsers(limit));
    }
}