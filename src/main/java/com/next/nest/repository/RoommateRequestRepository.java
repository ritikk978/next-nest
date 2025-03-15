package com.next.nest.repository;

import com.next.nest.entity.RoommateRequest;
import com.next.nest.entity.enums.Gender;
import com.next.nest.entity.enums.RoommateRequestStatus;
import com.next.nest.entity.enums.RoommateRequestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoommateRequestRepository extends JpaRepository<RoommateRequest, Long>,
        JpaSpecificationExecutor<RoommateRequest> {

    @Query(value = "SELECT * FROM roommate_requests WHERE user_id = :userId",
            countQuery = "SELECT COUNT(*) FROM roommate_requests WHERE user_id = :userId",
            nativeQuery = true)
    Page<RoommateRequest> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT * FROM roommate_requests WHERE status = :status",
            countQuery = "SELECT COUNT(*) FROM roommate_requests WHERE status = :status",
            nativeQuery = true)
    Page<RoommateRequest> findByStatus(@Param("status") String status, Pageable pageable);

    @Query(value = "SELECT * FROM roommate_requests WHERE request_type = :requestType",
            countQuery = "SELECT COUNT(*) FROM roommate_requests WHERE request_type = :requestType",
            nativeQuery = true)
    Page<RoommateRequest> findByRequestType(@Param("requestType") String requestType, Pageable pageable);

    @Query(value = "SELECT * FROM roommate_requests WHERE status = 'ACTIVE' AND " +
            "location = :location AND preferred_gender = :gender",
            countQuery = "SELECT COUNT(*) FROM roommate_requests WHERE status = 'ACTIVE' AND " +
                    "location = :location AND preferred_gender = :gender",
            nativeQuery = true)
    Page<RoommateRequest> findActiveRequestsByLocationAndGender(
            @Param("location") String location,
            @Param("gender") String gender,
            Pageable pageable);

    @Query(value = "SELECT * FROM roommate_requests WHERE status = 'ACTIVE' AND " +
            "location = :location AND budget <= :maxBudget",
            countQuery = "SELECT COUNT(*) FROM roommate_requests WHERE status = 'ACTIVE' AND " +
                    "location = :location AND budget <= :maxBudget",
            nativeQuery = true)
    Page<RoommateRequest> findActiveRequestsByLocationAndMaxBudget(
            @Param("location") String location,
            @Param("maxBudget") BigDecimal maxBudget,
            Pageable pageable);

    @Query(value = "SELECT * FROM roommate_requests WHERE status = 'ACTIVE' AND " +
            "location = :location AND move_in_date >= :fromDate AND move_in_date <= :toDate",
            countQuery = "SELECT COUNT(*) FROM roommate_requests WHERE status = 'ACTIVE' AND " +
                    "location = :location AND move_in_date >= :fromDate AND move_in_date <= :toDate",
            nativeQuery = true)
    Page<RoommateRequest> findActiveRequestsByLocationAndMoveInDateRange(
            @Param("location") String location,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    @Query(value = "SELECT DISTINCT location FROM roommate_requests WHERE status = 'ACTIVE' ORDER BY location",
            nativeQuery = true)
    List<String> findDistinctActiveLocations();

    @Query(value = "SELECT * FROM roommate_requests WHERE status = :status AND move_in_date < :date",
            nativeQuery = true)
    List<RoommateRequest> findByStatusAndMoveInDateBefore(
            @Param("status") String status,
            @Param("date") LocalDate date);

    @Query(value = "SELECT COUNT(*) FROM roommate_requests WHERE status = :status",
            nativeQuery = true)
    Long countByStatus(@Param("status") String status);

    // Fix for the date comparison issue
    @Query(value = "SELECT COUNT(*) FROM roommate_requests WHERE DATE(created_at) = CURRENT_DATE",
            nativeQuery = true)
    Long countRequestsCreatedToday();
}