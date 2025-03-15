package com.next.nest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceProviderDTO {
    
    private Long id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Contact person is required")
    private String contactPerson;
    
    @NotBlank(message = "Contact email is required")
    @Email(message = "Email should be valid")
    private String contactEmail;
    
    @NotBlank(message = "Contact phone is required")
    private String contactPhone;
    
    private String description;
    
    private String logoUrl;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    @NotBlank(message = "City is required")
    private String city;
    
    private String website;
    
    @NotNull(message = "Verification status is required")
    private Boolean isVerified;
    
    private Double rating;
    
    private Integer ratingCount;
    
    private Set<String> serviceAreas;
    
    @NotNull(message = "Active status is required")
    private Boolean isActive;
    
    private String identificationNumber;
    
    private String registrationDocument;
    
    private String specializations;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}