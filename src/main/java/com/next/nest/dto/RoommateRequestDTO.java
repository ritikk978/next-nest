package com.next.nest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.next.nest.entity.enums.Gender;
import com.next.nest.entity.enums.RoommateRequestStatus;
import com.next.nest.entity.enums.RoommateRequestType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoommateRequestDTO {
    
    private Long id;
    
    private Long userId;
    
    private UserDTO user;
    
    @NotNull(message = "Request type is required")
    private RoommateRequestType requestType;
    
    private RoommateRequestStatus status;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    private String specificArea;
    
    @NotNull(message = "Budget is required")
    @Min(value = 1, message = "Budget must be greater than 0")
    private BigDecimal budget;
    
    private BigDecimal rentAmount;
    
    private String propertyType;
    
    private Integer bhkType;
    
    private Gender preferredGender;
    
    private String preferredAgeRange;
    
    @NotNull(message = "Non-smoker preference is required")
    private Boolean nonSmoker;
    
    @NotNull(message = "No pets preference is required")
    private Boolean noPets;
    
    private Set<String> lifestyle;
    
    @NotNull(message = "Move-in date is required")
    @FutureOrPresent(message = "Move-in date must be in the future or present")
    private LocalDate moveInDate;
    
    private List<String> imageUrls;
    
    private Boolean isVerified;
    
    private List<RoommateResponseDTO> responses;
    
    private Integer responseCount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}