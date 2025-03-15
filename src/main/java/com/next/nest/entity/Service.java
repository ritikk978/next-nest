package com.next.nest.entity;

import com.next.nest.entity.enums.ServiceCategory;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToMany;
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
@Table(name = "services")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Service extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceCategory category;

    @Column(nullable = false)
    private BigDecimal basePrice;

    @Column(nullable = false)
    private Integer durationInMinutes;

    @ElementCollection
    private List<String> imageUrls = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal rating;

    @Column(nullable = false)
    private Integer ratingCount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceStatus status;

    @Column(nullable = false)
    private boolean featured;

    @ElementCollection
    private Set<String> serviceAreas = new HashSet<>();

    @ManyToMany
    private Set<ServiceProvider> serviceProviders = new HashSet<>();

    @OneToMany(mappedBy = "service")
    private List<Transaction> transactions = new ArrayList<>();

    @Column(nullable = false)
    private boolean availableForOnlineBooking;

    private BigDecimal discountPercentage;

    @Column(nullable = false)
    private boolean taxIncluded;

    private String termsAndConditions;

    private String cancellationPolicy;
}