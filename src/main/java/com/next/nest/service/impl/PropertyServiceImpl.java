package com.next.nest.service.impl;

import com.next.nest.dto.PropertyDTO;
import com.next.nest.dto.UserDTO;
import com.next.nest.entity.Property;
import com.next.nest.entity.User;
import com.next.nest.entity.enums.ListingStatus;
import com.next.nest.entity.enums.PropertyType;
import com.next.nest.exception.BadRequestException;
import com.next.nest.exception.ResourceNotFoundException;
import com.next.nest.exception.UnauthorizedException;
import com.next.nest.repository.BookingRepository;
import com.next.nest.repository.PropertyRepository;
import com.next.nest.repository.UserRepository;
import com.next.nest.service.FileStorageService;
import com.next.nest.service.PropertyService;
import com.next.nest.service.UserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final FileStorageService fileStorageService;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public PropertyDTO findById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        return mapToDTO(property);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyDTO> findAll(int page, int size) {
        return propertyRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyDTO> findAllActive(int page, int size) {
        return propertyRepository.findByIsActiveTrue(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyDTO> findByOwnerId(Long ownerId, int page, int size) {
        return propertyRepository.findByOwnerIdAndIsActiveTrue(ownerId, 
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyDTO> findByCity(String city, int page, int size) {
        return propertyRepository.findByCityAndActiveStatus(city, 
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyDTO> findByCityAndLocality(String city, String locality, int page, int size) {
        return propertyRepository.findByCityAndLocalityAndActiveStatus(city, locality, 
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyDTO> findByFilters(String city, PropertyType propertyType, Integer bhkType,
                                           BigDecimal minRent, BigDecimal maxRent,
                                           Map<String, Object> additionalFilters, int page, int size) {
        
        Specification<Property> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(criteriaBuilder.equal(root.get("isActive"), true));
            predicates.add(criteriaBuilder.equal(root.get("status"), ListingStatus.ACTIVE));
            
            if (city != null) {
                predicates.add(criteriaBuilder.equal(root.get("city"), city));
            }
            
            if (propertyType != null) {
                predicates.add(criteriaBuilder.equal(root.get("propertyType"), propertyType));
            }
            
            if (bhkType != null) {
                predicates.add(criteriaBuilder.equal(root.get("bhkType"), bhkType));
            }
            
            if (minRent != null && maxRent != null) {
                predicates.add(criteriaBuilder.between(root.get("rentAmount"), minRent, maxRent));
            } else if (minRent != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("rentAmount"), minRent));
            } else if (maxRent != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("rentAmount"), maxRent));
            }
            
            // Process additional filters
            if (additionalFilters != null) {
                if (additionalFilters.containsKey("furnishingStatus")) {
                    predicates.add(criteriaBuilder.equal(root.get("furnishingStatus"), 
                            additionalFilters.get("furnishingStatus")));
                }
                
                if (additionalFilters.containsKey("isReadyToMove") && additionalFilters.get("isReadyToMove") != null) {
                    predicates.add(criteriaBuilder.equal(root.get("isReadyToMove"), 
                            additionalFilters.get("isReadyToMove")));
                }
                
                if (additionalFilters.containsKey("isPetFriendly") && additionalFilters.get("isPetFriendly") != null) {
                    predicates.add(criteriaBuilder.equal(root.get("isPetFriendly"), 
                            additionalFilters.get("isPetFriendly")));
                }
                
                if (additionalFilters.containsKey("parkingAvailable") && additionalFilters.get("parkingAvailable") != null) {
                    predicates.add(criteriaBuilder.equal(root.get("parkingAvailable"), 
                            additionalFilters.get("parkingAvailable")));
                }
                
                if (additionalFilters.containsKey("propertyAge") && additionalFilters.get("propertyAge") != null) {
                    Integer maxAge = (Integer) additionalFilters.get("propertyAge");
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("propertyAge"), maxAge));
                }
                
                if (additionalFilters.containsKey("preferredTenantType") && additionalFilters.get("preferredTenantType") != null) {
                    predicates.add(criteriaBuilder.equal(root.get("preferredTenantType"), 
                            additionalFilters.get("preferredTenantType")));
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        return propertyRepository.findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyDTO> findByCoordinates(Double latitude, Double longitude, Double radius, 
                                           Map<String, Object> filters, int page, int size) {
        
        if (latitude == null || longitude == null || radius == null) {
            throw new BadRequestException("Latitude, longitude, and radius are required");
        }
        
        // Convert radius from km to meters
        Double radiusInMeters = radius * 1000;
        
        return propertyRepository.findPropertiesWithinRadius(latitude, longitude, radiusInMeters, 
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public PropertyDTO create(PropertyDTO propertyDTO) {
        User owner = getCurrentUser();
        
        Property property = Property.builder()
                .title(propertyDTO.getTitle())
                .description(propertyDTO.getDescription())
                .propertyType(propertyDTO.getPropertyType())
                .bhkType(propertyDTO.getBhkType())
                .rentAmount(propertyDTO.getRentAmount())
                .securityDeposit(propertyDTO.getSecurityDeposit())
                .maintenanceCharges(propertyDTO.getMaintenanceCharges())
                .lockInPeriod(propertyDTO.getLockInPeriod())
                .squareFeet(propertyDTO.getSquareFeet())
                .city(propertyDTO.getCity())
                .locality(propertyDTO.getLocality())
                .fullAddress(propertyDTO.getFullAddress())
                .projectName(propertyDTO.getProjectName())
                .latitude(propertyDTO.getLatitude())
                .longitude(propertyDTO.getLongitude())
                .furnishingStatus(propertyDTO.getFurnishingStatus())
                .ownershipType(propertyDTO.getOwnershipType())
                .floorNumber(propertyDTO.getFloorNumber())
                .totalFloors(propertyDTO.getTotalFloors())
                .propertyAge(propertyDTO.getPropertyAge())
                .parkingAvailable(propertyDTO.getParkingAvailable())
                .preferredTenantType(propertyDTO.getPreferredTenantType())
                .status(ListingStatus.PENDING_VERIFICATION)
                .owner(owner)
                .isActive(true)
                .isVerified(false)
                .isReadyToMove(propertyDTO.getIsReadyToMove())
                .isPetFriendly(propertyDTO.getIsPetFriendly())
                .brokerage(propertyDTO.getBrokerage())
                .build();
        
        if (propertyDTO.getAmenities() != null) {
            property.setAmenities(new HashSet<>(propertyDTO.getAmenities()));
        }
        
        if (propertyDTO.getImageUrls() != null) {
            property.setImageUrls(new ArrayList<>(propertyDTO.getImageUrls()));
        }
        
        Property savedProperty = propertyRepository.save(property);
        
        return mapToDTO(savedProperty);
    }

    @Override
    @Transactional
    public PropertyDTO update(Long id, PropertyDTO propertyDTO) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        
        // Check if the current user is the owner
        User currentUser = getCurrentUser();
        if (!property.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to update this property");
        }
        
        property.setTitle(propertyDTO.getTitle());
        property.setDescription(propertyDTO.getDescription());
        property.setPropertyType(propertyDTO.getPropertyType());
        property.setBhkType(propertyDTO.getBhkType());
        property.setRentAmount(propertyDTO.getRentAmount());
        property.setSecurityDeposit(propertyDTO.getSecurityDeposit());
        property.setMaintenanceCharges(propertyDTO.getMaintenanceCharges());
        property.setLockInPeriod(propertyDTO.getLockInPeriod());
        property.setSquareFeet(propertyDTO.getSquareFeet());
        property.setCity(propertyDTO.getCity());
        property.setLocality(propertyDTO.getLocality());
        property.setFullAddress(propertyDTO.getFullAddress());
        property.setProjectName(propertyDTO.getProjectName());
        property.setLatitude(propertyDTO.getLatitude());
        property.setLongitude(propertyDTO.getLongitude());
        property.setFurnishingStatus(propertyDTO.getFurnishingStatus());
        property.setOwnershipType(propertyDTO.getOwnershipType());
        property.setFloorNumber(propertyDTO.getFloorNumber());
        property.setTotalFloors(propertyDTO.getTotalFloors());
        property.setPropertyAge(propertyDTO.getPropertyAge());
        property.setParkingAvailable(propertyDTO.getParkingAvailable());
        property.setPreferredTenantType(propertyDTO.getPreferredTenantType());
        property.setIsReadyToMove(propertyDTO.getIsReadyToMove());
        property.setIsPetFriendly(propertyDTO.getIsPetFriendly());
        property.setBrokerage(propertyDTO.getBrokerage());
        
        // If property status is changed, set back to pending verification
        if (property.getStatus() == ListingStatus.ACTIVE) {
            property.setStatus(ListingStatus.PENDING_VERIFICATION);
            property.setIsVerified(false);
        }
        
        if (propertyDTO.getAmenities() != null) {
            property.setAmenities(new HashSet<>(propertyDTO.getAmenities()));
        }
        
        Property updatedProperty = propertyRepository.save(property);
        
        return mapToDTO(updatedProperty);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        
        // Check if the current user is the owner
        User currentUser = getCurrentUser();
        if (!property.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this property");
        }
        
        // Instead of hard delete, just set as inactive
        property.setIsActive(false);
        property.setStatus(ListingStatus.INACTIVE);
        propertyRepository.save(property);
    }

    @Override
    @Transactional
    public void changeStatus(Long id, ListingStatus status) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        
        // Check if the current user is the owner
        User currentUser = getCurrentUser();
        if (!property.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to change the status of this property");
        }
        
        property.setStatus(status);
        propertyRepository.save(property);
    }

    @Override
    @Transactional
    public PropertyDTO addImages(Long id, List<MultipartFile> images) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        
        // Check if the current user is the owner
        User currentUser = getCurrentUser();
        if (!property.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to add images to this property");
        }
        
        List<String> imageUrls = new ArrayList<>();
        
        for (MultipartFile image : images) {
            String imageUrl = fileStorageService.storeFile(image, "properties/" + id);
            imageUrls.add(imageUrl);
        }
        
        if (property.getImageUrls() == null) {
            property.setImageUrls(new ArrayList<>());
        }
        
        property.getImageUrls().addAll(imageUrls);
        Property updatedProperty = propertyRepository.save(property);
        
        return mapToDTO(updatedProperty);
    }

    @Override
    @Transactional
    public void removeImage(Long id, String imageUrl) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        
        // Check if the current user is the owner
        User currentUser = getCurrentUser();
        if (!property.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to remove images from this property");
        }
        
        if (property.getImageUrls() != null) {
            property.getImageUrls().remove(imageUrl);
            propertyRepository.save(property);
            
            // Delete file from storage
            fileStorageService.deleteFile(imageUrl);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctCities() {
        return propertyRepository.findDistinctCities();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctLocalitiesByCity(String city) {
        return propertyRepository.findDistinctLocalitiesByCity(city);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPropertyStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalActive", propertyRepository.countByStatus(ListingStatus.ACTIVE.name()));
        stats.put("totalPending", propertyRepository.countByStatus(ListingStatus.PENDING_VERIFICATION.name()));
        stats.put("totalRented", propertyRepository.countByStatus(ListingStatus.RENTED.name()));
        stats.put("addedToday", propertyRepository.countPropertiesAddedToday());
        
        return stats;
    }

    @Override
    @Transactional
    public void verifyProperty(Long id, boolean isVerified, String notes) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        
        property.setIsVerified(isVerified);
        property.setVerificationNotes(notes);
        
        if (isVerified) {
            property.setStatus(ListingStatus.ACTIVE);
        } else {
            property.setStatus(ListingStatus.REJECTED);
        }
        
        propertyRepository.save(property);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyDTO> findRecentlyAddedProperties(int limit) {
        return propertyRepository.findAll(PageRequest.of(0, limit, Sort.by("createdAt").descending()))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyDTO> searchProperties(String query, Map<String, Object> filters, int page, int size) {
        Specification<Property> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(criteriaBuilder.equal(root.get("isActive"), true));
            predicates.add(criteriaBuilder.equal(root.get("status"), ListingStatus.ACTIVE));
            
            if (query != null && !query.trim().isEmpty()) {
                String searchTerm = "%" + query.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("locality")), searchTerm),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("projectName")), searchTerm)
                ));
            }
            
            // Apply additional filters
            if (filters != null) {
                applyFilters(filters, root, criteriaBuilder, predicates);
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        return propertyRepository.findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyDTO> findByAmenities(List<String> amenities, Map<String, Object> filters, int page, int size) {
        if (amenities == null || amenities.isEmpty()) {
            throw new BadRequestException("Amenities list cannot be empty");
        }
        
        Specification<Property> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(criteriaBuilder.equal(root.get("isActive"), true));
            predicates.add(criteriaBuilder.equal(root.get("status"), ListingStatus.ACTIVE));
            
            // For each amenity, check if it's in the amenities set
            for (String amenity : amenities) {
                predicates.add(criteriaBuilder.isMember(amenity, root.get("amenities")));
            }
            
            // Apply additional filters
            if (filters != null) {
                applyFilters(filters, root, criteriaBuilder, predicates);
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        return propertyRepository.findAll(spec, PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }
    
    private void applyFilters(Map<String, Object> filters, jakarta.persistence.criteria.Root<Property> root, 
                            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder, 
                            List<Predicate> predicates) {
        
        if (filters.containsKey("city")) {
            predicates.add(criteriaBuilder.equal(root.get("city"), filters.get("city")));
        }
        
        if (filters.containsKey("propertyType")) {
            predicates.add(criteriaBuilder.equal(root.get("propertyType"), filters.get("propertyType")));
        }
        
        if (filters.containsKey("bhkType")) {
            predicates.add(criteriaBuilder.equal(root.get("bhkType"), filters.get("bhkType")));
        }
        
        if (filters.containsKey("minRent") && filters.containsKey("maxRent")) {
            predicates.add(criteriaBuilder.between(root.get("rentAmount"), 
                    (BigDecimal) filters.get("minRent"), (BigDecimal) filters.get("maxRent")));
        } else if (filters.containsKey("minRent")) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("rentAmount"), 
                    (BigDecimal) filters.get("minRent")));
        } else if (filters.containsKey("maxRent")) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("rentAmount"), 
                    (BigDecimal) filters.get("maxRent")));
        }
        
        if (filters.containsKey("furnishingStatus")) {
            predicates.add(criteriaBuilder.equal(root.get("furnishingStatus"), 
                    filters.get("furnishingStatus")));
        }
        
        if (filters.containsKey("isReadyToMove")) {
            predicates.add(criteriaBuilder.equal(root.get("isReadyToMove"), 
                    filters.get("isReadyToMove")));
        }
        
        if (filters.containsKey("isPetFriendly")) {
            predicates.add(criteriaBuilder.equal(root.get("isPetFriendly"), 
                    filters.get("isPetFriendly")));
        }
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
    
    private PropertyDTO mapToDTO(Property property) {
        PropertyDTO dto = PropertyDTO.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .propertyType(property.getPropertyType())
                .bhkType(property.getBhkType())
                .rentAmount(property.getRentAmount())
                .securityDeposit(property.getSecurityDeposit())
                .maintenanceCharges(property.getMaintenanceCharges())
                .lockInPeriod(property.getLockInPeriod())
                .squareFeet(property.getSquareFeet())
                .city(property.getCity())
                .locality(property.getLocality())
                .fullAddress(property.getFullAddress())
                .projectName(property.getProjectName())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .furnishingStatus(property.getFurnishingStatus())
                .ownershipType(property.getOwnershipType())
                .floorNumber(property.getFloorNumber())
                .totalFloors(property.getTotalFloors())
                .propertyAge(property.getPropertyAge())
                .parkingAvailable(property.getParkingAvailable())
                .preferredTenantType(property.getPreferredTenantType())
                .status(property.getStatus())
                .ownerId(property.getOwner().getId())
                .amenities(property.getAmenities())
                .imageUrls(property.getImageUrls())
                .isActive(property.getIsActive())
                .isVerified(property.getIsVerified())
                .verificationNotes(property.getVerificationNotes())
                .isReadyToMove(property.getIsReadyToMove())
                .isPetFriendly(property.getIsPetFriendly())
                .brokerage(property.getBrokerage())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .build();
        
        // Include owner details
        UserDTO ownerDTO = UserDTO.builder()
                .id(property.getOwner().getId())
                .firstName(property.getOwner().getFirstName())
                .lastName(property.getOwner().getLastName())
                .email(property.getOwner().getEmail())
                .phoneNumber(property.getOwner().getPhoneNumber())
                .profileImageUrl(property.getOwner().getProfileImageUrl())
                .role(property.getOwner().getRole())
                .build();
        
        dto.setOwner(ownerDTO);
        
        // Add booking and maintenance request counts if needed
        if (property.getBookings() != null) {
            dto.setBookingCount((long) property.getBookings().size());
        }
        
        if (property.getMaintenanceRequests() != null) {
            dto.setMaintenanceRequestCount((long) property.getMaintenanceRequests().size());
        }
        
        return dto;
    }
}