package com.next.nest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.next.nest.entity.enums.RoommateResponseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoommateResponseDTO {
    
    private Long id;
    
    @NotNull(message = "Roommate request ID is required")
    private Long roommateRequestId;
    
    private RoommateRequestDTO roommateRequest;
    
    private Long responderId;
    
    private UserDTO responder;
    
    @NotBlank(message = "Message is required")
    private String message;
    
    private RoommateResponseStatus status;
    
    private boolean isRead;
    
    private String contactInformation;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}