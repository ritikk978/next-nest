package com.next.nest.entity;

import com.next.nest.entity.enums.BookingStatus;
import com.next.nest.entity.enums.BookingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private User tenant;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingType bookingType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    private LocalDateTime confirmedTime;

    private String cancellationReason;

    private String notes;

    private boolean isPriority;

    @OneToMany(mappedBy = "booking")
    private List<Transaction> transactions = new ArrayList<>();

    private String contactName;

    private String contactEmail;

    private String contactPhone;

    private boolean isOfflineVisit;

    private boolean requiresAgentAssistance;

    @Column(length = 500)
    private String tenantRequirements;

    @Column(length = 500)
    private String feedbackFromTenant;

    private Integer ratingFromTenant;

    private LocalDateTime completedAt;
}