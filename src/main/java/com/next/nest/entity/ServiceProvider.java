package com.next.nest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "service_providers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProvider extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contactPerson;

    @Column(nullable = false)
    private String contactEmail;

    @Column(nullable = false)
    private String contactPhone;

    @Column(length = 1000)
    private String description;

    private String logoUrl;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    private String website;

    @Column(nullable = false)
    private Boolean isVerified;

    @Column(nullable = false)
    private Double rating;

    @Column(nullable = false)
    private Integer ratingCount;

    @ElementCollection
    private Set<String> serviceAreas = new HashSet<>();

    @ManyToMany(mappedBy = "serviceProviders")
    private Set<Service> services = new HashSet<>();

    @Column(nullable = false)
    private Boolean isActive;

    private String identificationNumber;

    private String registrationDocument;

    @Column(length = 500)
    private String specializations;
}