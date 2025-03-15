package com.next.nest.entity;

import com.next.nest.entity.enums.FurnishingStatus;
import com.next.nest.entity.enums.ListingStatus;
import com.next.nest.entity.enums.PropertyOwnershipType;
import com.next.nest.entity.enums.PropertyType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "properties")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyType propertyType;

    @Column(nullable = false)
    private Integer bhkType;

    @Column(nullable = false)
    private BigDecimal rentAmount;

    @Column(nullable = false)
    private BigDecimal securityDeposit;
    
    @Column(nullable = false)
    private BigDecimal maintenanceCharges;
    
    @Column(nullable = false)
    private Integer lockInPeriod; // in months
    
    @Column(nullable = false)
    private Double squareFeet;
    
    @Column(nullable = false)
    private String city;
    
    @Column(nullable = false)
    private String locality;
    
    @Column(nullable = false)
    private String fullAddress;
    
    @Column(nullable = false)
    private String projectName;
    
    private Double latitude;
    
    private Double longitude;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FurnishingStatus furnishingStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyOwnershipType ownershipType;
    
    private Integer floorNumber;
    
    private Integer totalFloors;
    
    @Column(nullable = false)
    private Integer propertyAge; // in years
    
    @Column(nullable = false)
    private Boolean parkingAvailable;
    
    @Column(nullable = false)
    private String preferredTenantType; // Family, Bachelor, etc.
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ListingStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    @ElementCollection
    private Set<String> amenities = new HashSet<>();
    
    @ElementCollection
    private List<String> imageUrls = new ArrayList<>();
    
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();
    
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaintenanceRequest> maintenanceRequests = new ArrayList<>();
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    private Boolean isVerified = false;
    
    private String verificationNotes;
    
    @Column(nullable = false)
    private Boolean isReadyToMove = true;
    
    @Column(nullable = false)
    private Boolean isPetFriendly = false;
    
    private Double brokerage;  // If applicable
}