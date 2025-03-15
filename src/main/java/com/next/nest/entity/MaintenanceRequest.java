package com.next.nest.entity;

import com.next.nest.entity.enums.MaintenanceStatus;
import com.next.nest.entity.enums.MaintenanceType;
import com.next.nest.entity.enums.UrgencyLevel;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "maintenance_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MaintenanceType type;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UrgencyLevel urgencyLevel;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status;

    @ElementCollection
    private List<String> imageUrls = new ArrayList<>();

    private LocalDateTime preferredDateTime;

    private LocalDateTime scheduledDateTime;

    private LocalDateTime completedDateTime;

    @Column(length = 1000)
    private String resolutionNotes;

    @Column(length = 1000)
    private String landlordNotes;

    private String assignedServiceProvider;

    private String contactNumber;

    private Integer satisfactionRating;

    @Column(length = 1000)
    private String feedback;

    private boolean visibleToLandlord;

    private boolean visibleToTenant;
}