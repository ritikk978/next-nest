package com.next.nest.service.impl;

import com.next.nest.dto.BookingDTO;
import com.next.nest.dto.PropertyDTO;
import com.next.nest.dto.UserDTO;
import com.next.nest.entity.Booking;
import com.next.nest.entity.Property;
import com.next.nest.entity.User;
import com.next.nest.entity.enums.BookingStatus;
import com.next.nest.entity.enums.BookingType;
import com.next.nest.entity.enums.UserRole;
import com.next.nest.exception.BadRequestException;
import com.next.nest.exception.ResourceNotFoundException;
import com.next.nest.exception.UnauthorizedException;
import com.next.nest.repository.BookingRepository;
import com.next.nest.repository.PropertyRepository;
import com.next.nest.repository.UserRepository;
import com.next.nest.service.BookingService;
import com.next.nest.util.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public BookingDTO findById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        // Security check: only the tenant, property owner, or admin can access the booking
        if (!isAuthorizedToAccessBooking(booking)) {
            throw new UnauthorizedException("You are not authorized to access this booking");
        }

        return mapToDTO(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDTO> findByTenantId(Long tenantId, int page, int size) {
        // Security check: only the tenant or admin can access their bookings
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(tenantId) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to access these bookings");
        }

        return bookingRepository.findByTenantId(tenantId,
                        PageRequest.of(page, size, Sort.by("scheduledTime").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDTO> findByPropertyOwnerId(Long ownerId, int page, int size) {
        // Security check: only the property owner or admin can access these bookings
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(ownerId) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to access these bookings");
        }

        return bookingRepository.findByPropertyOwnerId(ownerId,
                        PageRequest.of(page, size, Sort.by("scheduledTime").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDTO> findByPropertyId(Long propertyId, int page, int size) {
        // Verify property exists
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        // Security check: only the property owner or admin can access these bookings
        User currentUser = getCurrentUser();
        if (!property.getOwner().getId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to access these bookings");
        }

        return bookingRepository.findByPropertyIdAndStatus(propertyId, null,
                        PageRequest.of(page, size, Sort.by("scheduledTime").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDTO> findByPropertyIdAndStatus(Long propertyId, BookingStatus status, int page, int size) {
        // Verify property exists
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        // Security check: only the property owner or admin can access these bookings
        User currentUser = getCurrentUser();
        if (!property.getOwner().getId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to access these bookings");
        }

        return bookingRepository.findByPropertyIdAndStatus(propertyId, status.name(),
                        PageRequest.of(page, size, Sort.by("scheduledTime").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDTO> findByTenantIdAndStatus(Long tenantId, BookingStatus status, int page, int size) {
        // Security check: only the tenant or admin can access their bookings
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(tenantId) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to access these bookings");
        }

        return bookingRepository.findByTenantIdAndStatus(tenantId, status.name(),
                        PageRequest.of(page, size, Sort.by("scheduledTime").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDTO> findByPropertyOwnerIdAndStatus(Long ownerId, BookingStatus status, int page, int size) {
        // Security check: only the property owner or admin can access these bookings
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(ownerId) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to access these bookings");
        }

        return bookingRepository.findByPropertyOwnerIdAndStatus(ownerId, status.name(),
                        PageRequest.of(page, size, Sort.by("scheduledTime").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public BookingDTO create(BookingDTO bookingDTO) {
        // Get the current user (tenant)
        User tenant = getCurrentUser();

        // Verify property exists
        Property property = propertyRepository.findById(bookingDTO.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + bookingDTO.getPropertyId()));

        // Check if the time slot is available
        if (!isTimeSlotAvailable(property.getId(), bookingDTO.getScheduledTime(),
                bookingDTO.getScheduledTime().plusMinutes(30))) {
            throw new BadRequestException("This time slot is not available");
        }

        // Create booking
        Booking booking = Booking.builder()
                .property(property)
                .tenant(tenant)
                .bookingType(bookingDTO.getBookingType())
                .status(BookingStatus.PENDING)
                .scheduledTime(bookingDTO.getScheduledTime())
                .notes(bookingDTO.getNotes())
                .isPriority(bookingDTO.isPriority())
                .contactName(bookingDTO.getContactName())
                .contactEmail(bookingDTO.getContactEmail())
                .contactPhone(bookingDTO.getContactPhone())
                .isOfflineVisit(bookingDTO.isOfflineVisit())
                .requiresAgentAssistance(bookingDTO.isRequiresAgentAssistance())
                .tenantRequirements(bookingDTO.getTenantRequirements())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // Send notification emails
        sendBookingNotificationEmails(savedBooking);

        return mapToDTO(savedBooking);
    }

    @Override
    @Transactional
    public BookingDTO update(Long id, BookingDTO bookingDTO) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        // Security check: only the tenant can update their booking
        User currentUser = getCurrentUser();
        if (!booking.getTenant().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to update this booking");
        }

        // Verify booking is not already completed or cancelled
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cannot update a completed or cancelled booking");
        }

        // Check if changing scheduled time
        if (!booking.getScheduledTime().equals(bookingDTO.getScheduledTime())) {
            // Check if the new time slot is available
            if (!isTimeSlotAvailable(booking.getProperty().getId(), bookingDTO.getScheduledTime(),
                    bookingDTO.getScheduledTime().plusMinutes(30))) {
                throw new BadRequestException("This time slot is not available");
            }

            booking.setScheduledTime(bookingDTO.getScheduledTime());
        }

        booking.setNotes(bookingDTO.getNotes());
        booking.setPriority(bookingDTO.isPriority());
        booking.setContactName(bookingDTO.getContactName());
        booking.setContactEmail(bookingDTO.getContactEmail());
        booking.setContactPhone(bookingDTO.getContactPhone());
        booking.setOfflineVisit(bookingDTO.isOfflineVisit());
        booking.setRequiresAgentAssistance(bookingDTO.isRequiresAgentAssistance());
        booking.setTenantRequirements(bookingDTO.getTenantRequirements());

        Booking updatedBooking = bookingRepository.save(booking);

        return mapToDTO(updatedBooking);
    }

    @Override
    @Transactional
    public BookingDTO changeStatus(Long id, BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        // Security check: tenant can only cancel their booking
        // Property owner can confirm, complete, or cancel the booking
        User currentUser = getCurrentUser();
        boolean isTenant = booking.getTenant().getId().equals(currentUser.getId());
        boolean isOwner = booking.getProperty().getOwner().getId().equals(currentUser.getId());

        if (status == BookingStatus.CANCELLED) {
            if (!isTenant && !isOwner && !isAdmin(currentUser)) {
                throw new UnauthorizedException("You are not authorized to cancel this booking");
            }
        } else if (status == BookingStatus.CONFIRMED || status == BookingStatus.COMPLETED) {
            if (!isOwner && !isAdmin(currentUser)) {
                throw new UnauthorizedException("You are not authorized to confirm or complete this booking");
            }
        } else {
            if (!isOwner && !isAdmin(currentUser)) {
                throw new UnauthorizedException("You are not authorized to change the status of this booking");
            }
        }

        booking.setStatus(status);

        if (status == BookingStatus.CONFIRMED) {
            booking.setConfirmedTime(LocalDateTime.now());
        } else if (status == BookingStatus.COMPLETED) {
            booking.setCompletedAt(LocalDateTime.now());
        }

        Booking updatedBooking = bookingRepository.save(booking);

        // Send status update email
        sendBookingStatusUpdateEmail(updatedBooking);

        return mapToDTO(updatedBooking);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        // Security check: only the tenant can delete their booking
        User currentUser = getCurrentUser();
        if (!booking.getTenant().getId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to delete this booking");
        }

        // Instead of hard delete, just cancel the booking
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason("Deleted by user");
        bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDTO> findByScheduledDateBetween(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return bookingRepository.findByScheduledTimeBetween(startDateTime, endDateTime)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDTO> findByPropertyIdAndScheduledDateBetween(Long propertyId, LocalDate startDate, LocalDate endDate) {
        // Verify property exists
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        // Security check: only the property owner or tenant with a booking can access these bookings
        User currentUser = getCurrentUser();
        if (!property.getOwner().getId().equals(currentUser.getId()) &&
                !hasBookingForProperty(currentUser.getId(), propertyId) &&
                !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to access these bookings");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return bookingRepository.findByPropertyIdAndScheduledTimeBetween(propertyId, startDateTime, endDateTime)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTimeSlotAvailable(Long propertyId, LocalDateTime startTime, LocalDateTime endTime) {
        // Find bookings that overlap with the requested time slot
        LocalDateTime startDateTime = startTime.minusMinutes(30); // Buffer before
        LocalDateTime endDateTime = endTime.plusMinutes(30); // Buffer after

        List<Booking> overlappingBookings = bookingRepository.findByPropertyIdAndScheduledTimeBetween(
                propertyId, startDateTime, endDateTime);

        // Filter out cancelled bookings
        overlappingBookings = overlappingBookings.stream()
                .filter(booking -> booking.getStatus() != BookingStatus.CANCELLED)
                .collect(Collectors.toList());

        return overlappingBookings.isEmpty();
    }

    @Override
    @Transactional
    public void addFeedback(Long id, String feedback, Integer rating) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        // Security check: only the tenant can add feedback
        User currentUser = getCurrentUser();
        if (!booking.getTenant().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to add feedback to this booking");
        }

        // Verify booking is completed
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("Cannot add feedback to a booking that is not completed");
        }

        booking.setFeedbackFromTenant(feedback);
        booking.setRatingFromTenant(rating);

        bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBookingStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalPending", bookingRepository.countByTypeAndStatus(null, BookingStatus.PENDING.name()));
        stats.put("totalConfirmed", bookingRepository.countByTypeAndStatus(null, BookingStatus.CONFIRMED.name()));
        stats.put("totalCompleted", bookingRepository.countByTypeAndStatus(null, BookingStatus.COMPLETED.name()));
        stats.put("totalCancelled", bookingRepository.countByTypeAndStatus(null, BookingStatus.CANCELLED.name()));

        stats.put("pendingToday", countByStatusToday(BookingStatus.PENDING));
        stats.put("confirmedToday", countByStatusToday(BookingStatus.CONFIRMED));
        stats.put("completedToday", countByStatusToday(BookingStatus.COMPLETED));

        stats.put("propertyVisits", bookingRepository.countByTypeAndStatus(BookingType.PROPERTY_VISIT.name(), null));
        stats.put("virtualTours", bookingRepository.countByTypeAndStatus(BookingType.VIRTUAL_TOUR.name(), null));
        stats.put("propertyUnlocks", bookingRepository.countByTypeAndStatus(BookingType.PROPERTY_UNLOCK.name(), null));

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDTO> findUpcomingBookings(int limit) {
        return bookingRepository.findAll(PageRequest.of(0, limit, Sort.by("scheduledTime").ascending()))
                .stream()
                .filter(booking -> booking.getScheduledTime().isAfter(LocalDateTime.now()))
                .filter(booking -> booking.getStatus() != BookingStatus.CANCELLED)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatusToday(BookingStatus status) {
        return bookingRepository.countTodayBookingsByStatus(status.name());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByTypeAndStatus(BookingType type, BookingStatus status) {
        return bookingRepository.countByTypeAndStatus(type.name(), status.name());
    }

    private boolean isAuthorizedToAccessBooking(Booking booking) {
        User currentUser = getCurrentUser();

        // Admin can access all bookings
        if (isAdmin(currentUser)) {
            return true;
        }

        // Tenant can access their own bookings
        if (booking.getTenant().getId().equals(currentUser.getId())) {
            return true;
        }

        // Property owner can access bookings for their properties
        if (booking.getProperty().getOwner().getId().equals(currentUser.getId())) {
            return true;
        }

        return false;
    }

    private boolean hasBookingForProperty(Long userId, Long propertyId) {
        return bookingRepository.findByTenantId(userId, PageRequest.of(0, 10))
                .stream()
                .anyMatch(booking -> booking.getProperty().getId().equals(propertyId));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private boolean isAdmin(User user) {
        return user.getRole() == UserRole.ADMIN;
    }

    private void sendBookingNotificationEmails(Booking booking) {
        // Send to tenant
        String tenantName = booking.getTenant().getFirstName() + " " + booking.getTenant().getLastName();
        String propertyTitle = booking.getProperty().getTitle();
        String bookingDate = booking.getScheduledTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String bookingTime = booking.getScheduledTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        String bookingId = booking.getId().toString();

        emailService.sendBookingConfirmationEmail(
                booking.getTenant().getEmail(),
                tenantName,
                propertyTitle,
                bookingDate,
                bookingTime,
                bookingId
        );

        // Send to property owner
        String ownerName = booking.getProperty().getOwner().getFirstName() + " " + booking.getProperty().getOwner().getLastName();
        emailService.sendBookingConfirmationEmail(
                booking.getProperty().getOwner().getEmail(),
                ownerName,
                propertyTitle,
                bookingDate,
                bookingTime,
                bookingId
        );
    }

    private void sendBookingStatusUpdateEmail(Booking booking) {
        // Implement email notifications for status updates
        // Similar to sendBookingNotificationEmails but with status-specific messaging
    }

    private BookingDTO mapToDTO(Booking booking) {
        BookingDTO dto = BookingDTO.builder()
                .id(booking.getId())
                .propertyId(booking.getProperty().getId())
                .tenantId(booking.getTenant().getId())
                .bookingType(booking.getBookingType())
                .status(booking.getStatus())
                .scheduledTime(booking.getScheduledTime())
                .confirmedTime(booking.getConfirmedTime())
                .cancellationReason(booking.getCancellationReason())
                .notes(booking.getNotes())
                .isPriority(booking.isPriority())
                .contactName(booking.getContactName())
                .contactEmail(booking.getContactEmail())
                .contactPhone(booking.getContactPhone())
                .isOfflineVisit(booking.isOfflineVisit())
                .requiresAgentAssistance(booking.isRequiresAgentAssistance())
                .tenantRequirements(booking.getTenantRequirements())
                .feedbackFromTenant(booking.getFeedbackFromTenant())
                .ratingFromTenant(booking.getRatingFromTenant())
                .completedAt(booking.getCompletedAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();

        // Map property details
        PropertyDTO propertyDTO = PropertyDTO.builder()
                .id(booking.getProperty().getId())
                .title(booking.getProperty().getTitle())
                .propertyType(booking.getProperty().getPropertyType())
                .bhkType(booking.getProperty().getBhkType())
                .rentAmount(booking.getProperty().getRentAmount())
                .city(booking.getProperty().getCity())
                .locality(booking.getProperty().getLocality())
                .build();

        if (booking.getProperty().getImageUrls() != null && !booking.getProperty().getImageUrls().isEmpty()) {
            propertyDTO.setImageUrls(booking.getProperty().getImageUrls());
        }

        dto.setProperty(propertyDTO);

        // Map tenant details
        UserDTO tenantDTO = UserDTO.builder()
                .id(booking.getTenant().getId())
                .firstName(booking.getTenant().getFirstName())
                .lastName(booking.getTenant().getLastName())
                .email(booking.getTenant().getEmail())
                .phoneNumber(booking.getTenant().getPhoneNumber())
                .profileImageUrl(booking.getTenant().getProfileImageUrl())
                .build();

        dto.setTenant(tenantDTO);

        return dto;
    }
}