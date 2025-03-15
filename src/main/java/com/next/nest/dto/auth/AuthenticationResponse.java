package com.next.nest.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.next.nest.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationResponse {
    
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private String profileImageUrl;
    private boolean emailVerified;
    private boolean phoneVerified;
}