package com.next.nest.service;

import com.next.nest.dto.UserDTO;
import com.next.nest.dto.auth.AuthenticationRequest;
import com.next.nest.dto.auth.AuthenticationResponse;
import com.next.nest.dto.auth.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

public interface AuthService {
    
    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
    
    UserDTO getCurrentUser();
    
    void logout(HttpServletRequest request, HttpServletResponse response);
    
    boolean validateToken(String token);
}