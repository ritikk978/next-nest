package com.next.nest.service;

import com.next.nest.dto.RoommateRequestDTO;
import com.next.nest.dto.RoommateResponseDTO;
import com.next.nest.entity.enums.Gender;
import com.next.nest.entity.enums.RoommateRequestStatus;
import com.next.nest.entity.enums.RoommateRequestType;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface RoommateService {
    
    RoommateRequestDTO findRequestById(Long id);
    
    Page<RoommateRequestDTO> findAllActiveRequests(int page, int size);
    
    Page<RoommateRequestDTO> findRequestsByUserId(Long userId, int page, int size);
    
    Page<RoommateRequestDTO> findRequestsByStatus(RoommateRequestStatus status, int page, int size);
    
    Page<RoommateRequestDTO> findRequestsByType(RoommateRequestType type, int page, int size);
    
    Page<RoommateRequestDTO> findRequestsByLocationAndGender(String location, Gender gender, int page, int size);
    
    Page<RoommateRequestDTO> findRequestsByLocationAndMaxBudget(String location, BigDecimal maxBudget, int page, int size);
    
    Page<RoommateRequestDTO> findRequestsByLocationAndMoveInDateRange(
            String location, LocalDate fromDate, LocalDate toDate, int page, int size);
    
    RoommateRequestDTO createRequest(RoommateRequestDTO roommateRequestDTO);
    
    RoommateRequestDTO updateRequest(Long id, RoommateRequestDTO roommateRequestDTO);
    
    void deleteRequest(Long id);
    
    RoommateRequestDTO changeRequestStatus(Long id, RoommateRequestStatus status);
    
    RoommateRequestDTO addImagesToRequest(Long id, List<MultipartFile> images);
    
    void removeImageFromRequest(Long id, String imageUrl);
    
    List<String> getDistinctActiveLocations();
    
    RoommateResponseDTO findResponseById(Long id);
    
    Page<RoommateResponseDTO> findResponsesByRequestId(Long requestId, int page, int size);
    
    Page<RoommateResponseDTO> findResponsesByResponderId(Long responderId, int page, int size);
    
    RoommateResponseDTO createResponse(RoommateResponseDTO roommateResponseDTO);
    
    RoommateResponseDTO updateResponse(Long id, RoommateResponseDTO roommateResponseDTO);
    
    void deleteResponse(Long id);
    
    RoommateResponseDTO approveResponse(Long id);
    
    RoommateResponseDTO declineResponse(Long id);
    
    void markResponseAsRead(Long id);
    
    Map<String, Object> getRoommateStatistics();
}