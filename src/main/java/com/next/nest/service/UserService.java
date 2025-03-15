package com.next.nest.service;

import com.next.nest.dto.UserDTO;
import com.next.nest.entity.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    
    UserDTO findById(Long id);
    
    UserDTO findByEmail(String email);
    
    Page<UserDTO> findAll(int page, int size);
    
    Page<UserDTO> findAllByRole(UserRole role, int page, int size);
    
    Page<UserDTO> searchUsers(String searchTerm, UserRole role, int page, int size);
    
    UserDTO update(Long id, UserDTO userDTO);
    
    void delete(Long id);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    UserDTO findPropertyOwnerByPropertyId(Long propertyId);
    
    void updatePassword(Long userId, String oldPassword, String newPassword);
    
    void resetPassword(String email);
    
    void confirmResetPassword(String token, String newPassword);
    
    void verifyEmail(String token);
    
    void resendVerificationEmail(String email);
    
    void verifyPhone(String phoneNumber, String code);
    
    void sendPhoneVerificationCode(String phoneNumber);
    
    List<UserDTO> findRecentUsers(int limit);
    
    long countByRole(UserRole role);
    
    long countTodayRegistrations();
    
    UserDTO updateProfileImage(Long userId, String imageUrl);
}