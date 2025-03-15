package com.next.nest.service;

import com.next.nest.dto.PropertyDTO;
import com.next.nest.entity.enums.ListingStatus;
import com.next.nest.entity.enums.PropertyType;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PropertyService {
    
    PropertyDTO findById(Long id);
    
    Page<PropertyDTO> findAll(int page, int size);
    
    Page<PropertyDTO> findAllActive(int page, int size);
    
    Page<PropertyDTO> findByOwnerId(Long ownerId, int page, int size);
    
    Page<PropertyDTO> findByCity(String city, int page, int size);
    
    Page<PropertyDTO> findByCityAndLocality(String city, String locality, int page, int size);

    Page<PropertyDTO> findByFilters(String city, PropertyType propertyType, Integer bhkType,
                                   BigDecimal minRent, BigDecimal maxRent, Map<String, Object> additionalFilters,
                                   int page, int size);

    Page<PropertyDTO> findByCoordinates(Double latitude, Double longitude, Double radius,
                                        Map<String, Object> filters, int page, int size);
    
    PropertyDTO create(PropertyDTO propertyDTO);
    
    PropertyDTO update(Long id, PropertyDTO propertyDTO);

    void delete(Long id);
    
    void changeStatus(Long id, ListingStatus status);

    PropertyDTO addImages(Long id, List<MultipartFile> images);
    
    void removeImage(Long id, String imageUrl);
    
    List<String> getDistinctCities();
    
    List<String> getDistinctLocalitiesByCity(String city);
    
    Map<String, Object> getPropertyStatistics();
    
    void verifyProperty(Long id, boolean isVerified, String notes);
    
    List<PropertyDTO> findRecentlyAddedProperties(int limit);
    
    Page<PropertyDTO> searchProperties(String query, Map<String, Object> filters, int page, int size);
    
    Page<PropertyDTO> findByAmenities(List<String> amenities, Map<String, Object> filters, int page, int size);
}