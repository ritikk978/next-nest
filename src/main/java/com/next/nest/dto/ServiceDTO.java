package com.next.nest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.next.nest.entity.ServiceStatus;
import com.next.nest.entity.enums.ServiceCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceDTO {
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private ServiceCategory category;
    
    private BigDecimal basePrice;
    
    private Integer durationInMinutes;
    
    private List<String> imageUrls;
    
    private BigDecimal rating;
    
    private Integer ratingCount;
    
    private ServiceStatus status;
    
    private boolean featured;
    
    private Set<String> serviceAreas;
    
    private List<ServiceProviderDTO> serviceProviders;
    
    private boolean availableForOnlineBooking;
    
    private BigDecimal discountPercentage;
    
    private boolean taxIncluded;
    
    private String termsAndConditions;
    
    private String cancellationPolicy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}