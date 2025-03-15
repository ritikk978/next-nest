package com.next.nest.repository;

import com.next.nest.entity.Transaction;
import com.next.nest.entity.enums.PaymentStatus;
import com.next.nest.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(value = "SELECT * FROM transactions WHERE transaction_id = :transactionId", nativeQuery = true)
    Optional<Transaction> findByTransactionId(@Param("transactionId") String transactionId);

    @Query(value = "SELECT * FROM transactions WHERE user_id = :userId",
            countQuery = "SELECT COUNT(*) FROM transactions WHERE user_id = :userId",
            nativeQuery = true)
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT t.* FROM transactions t " +
            "JOIN bookings b ON t.booking_id = b.id " +
            "WHERE b.property_owner_id = :ownerId",
            countQuery = "SELECT COUNT(t.*) FROM transactions t " +
                    "JOIN bookings b ON t.booking_id = b.id " +
                    "WHERE b.property_owner_id = :ownerId",
            nativeQuery = true)
    Page<Transaction> findByBookingPropertyOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query(value = "SELECT * FROM transactions WHERE booking_id = :bookingId",
            countQuery = "SELECT COUNT(*) FROM transactions WHERE booking_id = :bookingId",
            nativeQuery = true)
    Page<Transaction> findByBookingId(@Param("bookingId") Long bookingId, Pageable pageable);

    @Query(value = "SELECT * FROM transactions WHERE user_id = :userId AND status = :status",
            countQuery = "SELECT COUNT(*) FROM transactions WHERE user_id = :userId AND status = :status",
            nativeQuery = true)
    Page<Transaction> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status, Pageable pageable);

    @Query(value = "SELECT * FROM transactions WHERE type = :type",
            countQuery = "SELECT COUNT(*) FROM transactions WHERE type = :type",
            nativeQuery = true)
    Page<Transaction> findByType(@Param("type") String type, Pageable pageable);

    @Query(value = "SELECT * FROM transactions WHERE status = :status AND " +
            "created_at BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    List<Transaction> findByStatusAndDateRange(
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT SUM(amount) FROM transactions WHERE status = 'SUCCESS' AND " +
            "type = :type AND created_at BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    Optional<BigDecimal> sumSuccessfulTransactionsByTypeAndDateRange(
            @Param("type") String type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Fix for the date comparison issue
    @Query(value = "SELECT COUNT(*) FROM transactions WHERE status = :status AND DATE(created_at) = CURRENT_DATE",
            nativeQuery = true)
    Long countTodayTransactionsByStatus(@Param("status") String status);

    @Query(value = "SELECT SUM(amount) FROM transactions WHERE status = 'SUCCESS' AND DATE(created_at) = CURRENT_DATE",
            nativeQuery = true)
    Optional<BigDecimal> sumTodaySuccessfulTransactions();

    @Query(value = "SELECT SUM(amount) FROM transactions WHERE status = 'SUCCESS' AND " +
            "type = :type AND EXTRACT(YEAR FROM created_at) = :year AND EXTRACT(MONTH FROM created_at) = :month",
            nativeQuery = true)
    Optional<BigDecimal> sumSuccessfulTransactionsByTypeAndMonth(
            @Param("type") String type,
            @Param("year") int year,
            @Param("month") int month);
}