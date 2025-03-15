package com.next.nest.service;

import com.next.nest.dto.BookingDTO;
import com.next.nest.entity.enums.BookingStatus;
import com.next.nest.entity.enums.BookingType;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface BookingService {
    
    BookingDTO findById(Long id);
    
    Page<BookingDTO> findByTenantId(Long tenantId, int page, int size);
    
    Page<BookingDTO> findByPropertyOwnerId(Long ownerId, int page, int size);
    
    Page<BookingDTO> findByPropertyId(Long propertyId, int page, int size);
    
    Page<BookingDTO> findByPropertyIdAndStatus(Long propertyId, BookingStatus status, int page, int size);
    
    Page<BookingDTO> findByTenantIdAndStatus(Long tenantId, BookingStatus status, int page, int size);
    
    Page<BookingDTO> findByPropertyOwnerIdAndStatus(Long ownerId, BookingStatus status, int page, int size);
    
    BookingDTO create(BookingDTO bookingDTO);
    
    BookingDTO update(Long id, BookingDTO bookingDTO);
    
    BookingDTO changeStatus(Long id, BookingStatus status);

    void delete(Long id);
    
    List<BookingDTO> findByScheduledDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<BookingDTO> findByPropertyIdAndScheduledDateBetween(Long propertyId, LocalDate startDate, LocalDate endDate);
    
    boolean isTimeSlotAvailable(Long propertyId, LocalDateTime startTime, LocalDateTime endTime);
    
    void addFeedback(Long id, String feedback, Integer rating);
    
    Map<String, Object> getBookingStatistics();
    
    List<BookingDTO> findUpcomingBookings(int limit);
    
    long countByStatusToday(BookingStatus status);
    
    long countByTypeAndStatus(BookingType type, BookingStatus status);
}