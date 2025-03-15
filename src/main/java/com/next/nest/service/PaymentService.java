package com.next.nest.service;

import com.next.nest.dto.TransactionDTO;
import com.next.nest.entity.enums.PaymentStatus;
import com.next.nest.entity.enums.TransactionType;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PaymentService {
    
    TransactionDTO findById(Long id);
    
    TransactionDTO findByTransactionId(String transactionId);
    
    Page<TransactionDTO> findByUserId(Long userId, int page, int size);
    
    Page<TransactionDTO> findByBookingPropertyOwnerId(Long ownerId, int page, int size);
    
    Page<TransactionDTO> findByBookingId(Long bookingId, int page, int size);
    
    Page<TransactionDTO> findByUserIdAndStatus(Long userId, PaymentStatus status, int page, int size);

    Page<TransactionDTO> findByType(TransactionType type, int page, int size);

    TransactionDTO initiatePayment(TransactionDTO transactionDTO);
    
    TransactionDTO completePayment(String transactionId, Map<String, Object> paymentDetails);
    
    TransactionDTO processPaymentCallback(Map<String, Object> callbackData);
    
    TransactionDTO initiateRefund(String transactionId, BigDecimal amount, String reason);
    
    TransactionDTO updateStatus(String transactionId, PaymentStatus status, String statusDetails);
    
    String generatePaymentReceipt(String transactionId);

    Map<String, Object> getPaymentStatisticsByDateRange(LocalDate startDate, LocalDate endDate);
    
    List<Map<String, Object>> getMonthlyRevenueData(int year);
    
    BigDecimal getTotalRevenue();
    
    BigDecimal getTotalRevenueByType(TransactionType type);
    
    long countSuccessfulTransactionsToday();
    
    BigDecimal sumSuccessfulTransactionsToday();
}