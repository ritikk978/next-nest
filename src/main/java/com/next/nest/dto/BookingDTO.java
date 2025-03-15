package com.next.nest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.next.nest.entity.enums.BookingStatus;
import com.next.nest.entity.enums.BookingType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDTO {
    
    private Long id;
    
    @NotNull(message = "Property ID is required")
    private Long propertyId;
    
    private PropertyDTO property;
    
    private Long tenantId;
    
    private UserDTO tenant;
    
    @NotNull(message = "Booking type is required")
    private BookingType bookingType;
    
    private BookingStatus status;
    
    @NotNull(message = "Scheduled time is required")
    @FutureOrPresent(message = "Scheduled time must be in the future or present")
    private LocalDateTime scheduledTime;
    
    private LocalDateTime confirmedTime;
    
    private String cancellationReason;
    
    private String notes;
    
    private boolean isPriority;
    
    private List<TransactionDTO> transactions;
    
    @NotNull(message = "Contact name is required")
    private String contactName;
    
    @NotNull(message = "Contact email is required")
    private String contactEmail;
    
    @NotNull(message = "Contact phone is required")
    private String contactPhone;
    
    private boolean isOfflineVisit;
    
    private boolean requiresAgentAssistance;
    
    private String tenantRequirements;
    
    private String feedbackFromTenant;
    
    private Integer ratingFromTenant;
    
    private LocalDateTime completedAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}