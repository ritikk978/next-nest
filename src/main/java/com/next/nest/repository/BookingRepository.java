package com.next.nest.repository;

import com.next.nest.entity.Booking;
import com.next.nest.entity.enums.BookingStatus;
import com.next.nest.entity.enums.BookingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(value = "SELECT * FROM bookings WHERE tenant_id = :tenantId",
            countQuery = "SELECT COUNT(*) FROM bookings WHERE tenant_id = :tenantId",
            nativeQuery = true)
    Page<Booking> findByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query(value = "SELECT * FROM bookings WHERE property_owner_id = :ownerId",
            countQuery = "SELECT COUNT(*) FROM bookings WHERE property_owner_id = :ownerId",
            nativeQuery = true)
    Page<Booking> findByPropertyOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query(value = "SELECT * FROM bookings WHERE property_id = :propertyId AND status = :status",
            countQuery = "SELECT COUNT(*) FROM bookings WHERE property_id = :propertyId AND status = :status",
            nativeQuery = true)
    Page<Booking> findByPropertyIdAndStatus(@Param("propertyId") Long propertyId,
                                            @Param("status") String status,
                                            Pageable pageable);

    @Query(value = "SELECT * FROM bookings WHERE tenant_id = :tenantId AND status = :status",
            countQuery = "SELECT COUNT(*) FROM bookings WHERE tenant_id = :tenantId AND status = :status",
            nativeQuery = true)
    Page<Booking> findByTenantIdAndStatus(@Param("tenantId") Long tenantId,
                                          @Param("status") String status,
                                          Pageable pageable);

    @Query(value = "SELECT b.* FROM bookings b " +
            "JOIN properties p ON b.property_id = p.id " +
            "WHERE p.owner_id = :ownerId AND b.status = :status",
            countQuery = "SELECT COUNT(b.*) FROM bookings b " +
                    "JOIN properties p ON b.property_id = p.id " +
                    "WHERE p.owner_id = :ownerId AND b.status = :status",
            nativeQuery = true)
    Page<Booking> findByPropertyOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                                 @Param("status") String status,
                                                 Pageable pageable);

    @Query(value = "SELECT * FROM bookings WHERE scheduled_time BETWEEN :startDateTime AND :endDateTime",
            nativeQuery = true)
    List<Booking> findByScheduledTimeBetween(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query(value = "SELECT * FROM bookings WHERE property_id = :propertyId AND " +
            "scheduled_time BETWEEN :startDateTime AND :endDateTime",
            nativeQuery = true)
    List<Booking> findByPropertyIdAndScheduledTimeBetween(
            @Param("propertyId") Long propertyId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    // Fixed the date comparison issue by explicitly casting to date in PostgreSQL
    @Query(value = "SELECT COUNT(*) FROM bookings WHERE status = :status AND DATE(created_at) = CURRENT_DATE",
            nativeQuery = true)
    Long countTodayBookingsByStatus(@Param("status") String status);

    @Query(value = "SELECT COUNT(*) FROM bookings WHERE booking_type = :type AND status = :status",
            nativeQuery = true)
    Long countByTypeAndStatus(@Param("type") String type, @Param("status") String status);

    @Query(value = "SELECT * FROM bookings WHERE status = :status AND scheduled_time < :dateTime",
            nativeQuery = true)
    List<Booking> findByStatusAndScheduledTimeBefore(
            @Param("status") String status,
            @Param("dateTime") LocalDateTime dateTime);
}