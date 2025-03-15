package com.next.nest.service.impl;

import com.next.nest.dto.UserDTO;
import com.next.nest.entity.User;
import com.next.nest.entity.enums.UserRole;
import com.next.nest.exception.*;
import com.next.nest.repository.UserRepository;
import com.next.nest.service.UserService;
import com.next.nest.util.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findAll(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findAllByRole(UserRole role, int page, int size) {
        return userRepository.findAllByRole(role.name(), PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> searchUsers(String searchTerm, UserRole role, int page, int size) {
        return userRepository.findAllByRoleAndSearchText(role.name(), searchTerm,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Check if email is being changed and if it's already taken
        if (!user.getEmail().equals(userDTO.getEmail()) && 
            userRepository.existsByEmail(userDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + userDTO.getEmail());
        }
        
        // Check if phone is being changed and if it's already taken
        if (!user.getPhoneNumber().equals(userDTO.getPhoneNumber()) && 
            userRepository.existsByPhoneNumber(userDTO.getPhoneNumber())) {
            throw new PhoneAlreadyExistsException("Phone number already exists: " + userDTO.getPhoneNumber());
        }
        
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        
        // If email is changed, set verified to false and send verification
        if (!user.getEmail().equals(userDTO.getEmail())) {
            user.setEmail(userDTO.getEmail());
            user.setEmailVerified(false);
            // Send verification email
            sendVerificationEmail(user);
        }
        
        // If phone number is changed, set verified to false
        if (!user.getPhoneNumber().equals(userDTO.getPhoneNumber())) {
            user.setPhoneNumber(userDTO.getPhoneNumber());
            user.setPhoneVerified(false);
        }
        
        if (userDTO.getDateOfBirth() != null) {
            user.setDateOfBirth(userDTO.getDateOfBirth());
        }
        
        if (userDTO.getGender() != null) {
            user.setGender(userDTO.getGender());
        }
        
        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Instead of hard delete, consider just setting account as inactive
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO findPropertyOwnerByPropertyId(Long propertyId) {
        User owner = userRepository.findPropertyOwnerByPropertyId(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property owner not found for property id: " + propertyId));
        return mapToDTO(owner);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        // Generate and save reset token
        String resetToken = UUID.randomUUID().toString();
        // Here you would save this token with expiration time 
        // For simplicity, we're just sending the email
        
        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    @Override
    @Transactional
    public void confirmResetPassword(String token, String newPassword) {
        // In a real implementation, you would validate the token
        // Find the user associated with the valid token
        // For this example, we'll just throw an exception
        throw new BadRequestException("Token validation not implemented in this example");
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        // In a real implementation, you would validate the token
        // Find the user associated with the valid token
        // For this example, we'll just throw an exception
        throw new BadRequestException("Email verification not implemented in this example");
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }
        
        sendVerificationEmail(user);
    }

    @Override
    @Transactional
    public void verifyPhone(String phoneNumber, String code) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone number: " + phoneNumber));
        
        // In a real implementation, you would validate the code
        // For this example, we'll just set the phone as verified
        user.setPhoneVerified(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void sendPhoneVerificationCode(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone number: " + phoneNumber));
        
        // In a real implementation, you would generate a code and send via SMS
        // For this example, we'll just log it
        log.info("Sending verification code to phone: {}", phoneNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> findRecentUsers(int limit) {
        return userRepository.findAll(PageRequest.of(0, limit, Sort.by("createdAt").descending()))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRole(UserRole role) {
        return userRepository.countByRole(role.name());
    }

    @Override
    @Transactional(readOnly = true)
    public long countTodayRegistrations() {
        return userRepository.countTodayRegistrations();
    }

    @Override
    @Transactional
    public UserDTO updateProfileImage(Long userId, String imageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        user.setProfileImageUrl(imageUrl);
        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }
    
    private void sendVerificationEmail(User user) {
        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();
        // In a real implementation, you would save this token with the user
        
        // Send email
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
    }
    
    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .faceIdRegistered(user.isFaceIdRegistered())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .propertyCount(user.getProperties() != null ? user.getProperties().size() : 0)
                .bookingCount(user.getBookings() != null ? user.getBookings().size() : 0)
                .build();
    }
}