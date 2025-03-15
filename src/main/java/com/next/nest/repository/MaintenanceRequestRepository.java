package com.next.nest.repository;

import com.next.nest.entity.MaintenanceRequest;
import com.next.nest.entity.enums.MaintenanceStatus;
import com.next.nest.entity.enums.MaintenanceType;
import com.next.nest.entity.enums.UrgencyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {

    @Query(value = "SELECT * FROM maintenance_requests WHERE property_id = :propertyId",
            countQuery = "SELECT COUNT(*) FROM maintenance_requests WHERE property_id = :propertyId",
            nativeQuery = true)
    Page<MaintenanceRequest> findByPropertyId(@Param("propertyId") Long propertyId, Pageable pageable);

    @Query(value = "SELECT * FROM maintenance_requests WHERE requester_id = :requesterId",
            countQuery = "SELECT COUNT(*) FROM maintenance_requests WHERE requester_id = :requesterId",
            nativeQuery = true)
    Page<MaintenanceRequest> findByRequesterId(@Param("requesterId") Long requesterId, Pageable pageable);

    @Query(value = "SELECT m.* FROM maintenance_requests m " +
            "JOIN properties p ON m.property_id = p.id " +
            "WHERE p.owner_id = :ownerId",
            countQuery = "SELECT COUNT(m.*) FROM maintenance_requests m " +
                    "JOIN properties p ON m.property_id = p.id " +
                    "WHERE p.owner_id = :ownerId",
            nativeQuery = true)
    Page<MaintenanceRequest> findByPropertyOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query(value = "SELECT * FROM maintenance_requests WHERE status = :status",
            countQuery = "SELECT COUNT(*) FROM maintenance_requests WHERE status = :status",
            nativeQuery = true)
    Page<MaintenanceRequest> findByStatus(@Param("status") String status, Pageable pageable);

    @Query(value = "SELECT * FROM maintenance_requests WHERE type = :type",
            countQuery = "SELECT COUNT(*) FROM maintenance_requests WHERE type = :type",
            nativeQuery = true)
    Page<MaintenanceRequest> findByType(@Param("type") String type, Pageable pageable);

    @Query(value = "SELECT * FROM maintenance_requests WHERE urgency_level = :urgencyLevel",
            countQuery = "SELECT COUNT(*) FROM maintenance_requests WHERE urgency_level = :urgencyLevel",
            nativeQuery = true)
    Page<MaintenanceRequest> findByUrgencyLevel(@Param("urgencyLevel") String urgencyLevel, Pageable pageable);

    @Query(value = "SELECT * FROM maintenance_requests WHERE property_id = :propertyId AND status = :status",
            countQuery = "SELECT COUNT(*) FROM maintenance_requests WHERE property_id = :propertyId AND status = :status",
            nativeQuery = true)
    Page<MaintenanceRequest> findByPropertyIdAndStatus(
            @Param("propertyId") Long propertyId,
            @Param("status") String status,
            Pageable pageable);

    @Query(value = "SELECT m.* FROM maintenance_requests m " +
            "JOIN properties p ON m.property_id = p.id " +
            "WHERE p.owner_id = :ownerId AND m.status = :status",
            countQuery = "SELECT COUNT(m.*) FROM maintenance_requests m " +
                    "JOIN properties p ON m.property_id = p.id " +
                    "WHERE p.owner_id = :ownerId AND m.status = :status",
            nativeQuery = true)
    Page<MaintenanceRequest> findByPropertyOwnerIdAndStatus(
            @Param("ownerId") Long ownerId,
            @Param("status") String status,
            Pageable pageable);

    @Query(value = "SELECT * FROM maintenance_requests WHERE scheduled_date_time BETWEEN :startDateTime AND :endDateTime",
            nativeQuery = true)
    List<MaintenanceRequest> findByScheduledDateTimeBetween(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query(value = "SELECT COUNT(*) FROM maintenance_requests WHERE status = :status",
            nativeQuery = true)
    Long countByStatus(@Param("status") String status);

    @Query(value = "SELECT COUNT(*) FROM maintenance_requests WHERE urgency_level = :urgencyLevel AND status IN ('REQUESTED', 'SCHEDULED')",
            nativeQuery = true)
    Long countActiveRequestsByUrgencyLevel(@Param("urgencyLevel") String urgencyLevel);

    // Fix for the date comparison issue
    @Query(value = "SELECT COUNT(*) FROM maintenance_requests WHERE DATE(created_at) = CURRENT_DATE",
            nativeQuery = true)
    Long countRequestsCreatedToday();

    @Query(value = "SELECT * FROM maintenance_requests WHERE status = :status AND scheduled_date_time < :dateTime",
            nativeQuery = true)
    List<MaintenanceRequest> findByStatusAndScheduledDateTimeBefore(
            @Param("status") String status,
            @Param("dateTime") LocalDateTime dateTime);
}