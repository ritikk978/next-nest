package com.next.nest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.next.nest.entity.enums.FurnishingStatus;
import com.next.nest.entity.enums.ListingStatus;
import com.next.nest.entity.enums.PropertyOwnershipType;
import com.next.nest.entity.enums.PropertyType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class PropertyDTO {
    
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 1000, message = "Description must be between 20 and 1000 characters")
    private String description;
    
    @NotNull(message = "Property type is required")
    private PropertyType propertyType;
    
    @NotNull(message = "BHK type is required")
    private Integer bhkType;
    
    @NotNull(message = "Rent amount is required")
    @Min(value = 1, message = "Rent amount must be greater than 0")
    private BigDecimal rentAmount;
    
    @NotNull(message = "Security deposit is required")
    @Min(value = 0, message = "Security deposit cannot be negative")
    private BigDecimal securityDeposit;
    
    @NotNull(message = "Maintenance charges are required")
    @Min(value = 0, message = "Maintenance charges cannot be negative")
    private BigDecimal maintenanceCharges;
    
    @NotNull(message = "Lock-in period is required")
    @Min(value = 0, message = "Lock-in period cannot be negative")
    private Integer lockInPeriod;
    
    @NotNull(message = "Square feet is required")
    @Min(value = 1, message = "Square feet must be greater than 0")
    private Double squareFeet;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "Locality is required")
    private String locality;
    
    @NotBlank(message = "Full address is required")
    private String fullAddress;
    
    @NotBlank(message = "Project name is required")
    private String projectName;
    
    private Double latitude;
    
    private Double longitude;
    
    @NotNull(message = "Furnishing status is required")
    private FurnishingStatus furnishingStatus;
    
    @NotNull(message = "Ownership type is required")
    private PropertyOwnershipType ownershipType;
    
    private Integer floorNumber;
    
    private Integer totalFloors;
    
    @NotNull(message = "Property age is required")
    private Integer propertyAge;
    
    @NotNull(message = "Parking availability is required")
    private Boolean parkingAvailable;
    
    @NotBlank(message = "Preferred tenant type is required")
    private String preferredTenantType;
    
    private ListingStatus status;
    
    private Long ownerId;
    
    private Set<String> amenities;
    
    private List<String> imageUrls;
    
    private Boolean isActive;
    
    private Boolean isVerified;
    
    private String verificationNotes;
    
    @NotNull(message = "Ready to move status is required")
    private Boolean isReadyToMove;
    
    @NotNull(message = "Pet friendly status is required")
    private Boolean isPetFriendly;
    
    private Double brokerage;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Additional fields for response
    private UserDTO owner;
    private Long bookingCount;
    private Long maintenanceRequestCount;
    private Long viewCount;
    private Long favoriteCount;
}