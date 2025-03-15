package com.next.nest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.next.nest.entity.enums.PaymentMethod;
import com.next.nest.entity.enums.PaymentStatus;
import com.next.nest.entity.enums.TransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDTO {
    
    private Long id;
    
    private String transactionId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private UserDTO user;
    
    private Long bookingId;
    
    private BookingDTO booking;
    
    private Long serviceId;
    
    private ServiceDTO service;
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private PaymentStatus status;
    
    private LocalDateTime paymentDate;
    
    private String referenceId;
    
    private String failureReason;
    
    private String description;
    
    private BigDecimal fees;
    
    private BigDecimal tax;
    
    private BigDecimal totalAmount;
    
    private boolean isRefundable;
    
    private String receiptUrl;
    
    private String paymentGatewayResponse;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}